package kolskypavel.ardfmanager.backend.sportident

import android.util.Log
import androidx.lifecycle.Observer
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.sportident.SIConstants.GET_SI_CARD8_9_SIAC
import kolskypavel.ardfmanager.backend.sportident.SIConstants.HALF_DAY
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD10_11_SIAC_MAX_PUNCHES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD10_11_SIAC_SERIES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD8_MAX_PUNCHES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD8_SERIES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD9_MAX_PUNCHES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD9_SERIES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD_PCARD_MAX_PUNCHES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_CARD_PCARD_SERIES
import kolskypavel.ardfmanager.backend.sportident.SIConstants.ZERO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.min
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import kotlin.experimental.and

class SIPort(
    private val port: UsbSerialDevice
) {

    val dataProcessor = DataProcessor.get()
    private val msgCache: ArrayList<ByteArray> = ArrayList()
    private var extendedMode =
        false                            //  Marks if station uses SI extended mode
    private var serialNo: Int = 0                           //  Serial number of the SI station
    private var readerStatus = SIReaderStatus.DISCONNECTED

    private var event: Event? = null
    private var zeroTimeBase = 0L
    private var zeroTimeWeekDay = 0L
    private var observer: Observer<Event>? = null

    /**
     * Stores the temp readout data
     */
    inner class CardData(
        var cardType: Byte,
        var siNumber: Int,
        var checkTime: Long,
        var startTime: Long,
        var finishTime: Long,
        var punchData: ArrayList<PunchData>
    )

    /**
     * Stores the temp punch data
     */
    inner class PunchData(var siCode: Int, var time: Long)

    fun workJob(): Job {
        val job = CoroutineScope(Dispatchers.IO).launch {

            setEventObserver()

            while (true) {
                delay(SIConstants.DELAY_INTERVAL)

                if (readerStatus == SIReaderStatus.DISCONNECTED && probeDevice()) {
                    setStatusConnected()
                }
                if (readerStatus == SIReaderStatus.CONNECTED) {
                    readCardOnce()
                }
            }
        }
        return job
    }

    /**
     * Sets the event and adjusts the timebase for the SI time calculation
     */
    private fun setEventObserver() {
        observer = Observer { newEvent ->
            event = newEvent
            this.zeroTimeBase =
                ((event!!.startTime.hour * 3600000) + (event!!.startTime.minute * 60000) + (event!!.startTime.second * 1000)).toLong()
            this.zeroTimeWeekDay = (event!!.date.dayOfWeek.value % 7).toLong()
        }
        //   dataProcessor.currentEvent.observeForever(observer!!)
    }

    private fun removeObserver() {
        if (observer != null) {
            dataProcessor.currentEvent.removeObserver(observer!!)
        }
    }

    /**
     * Write the given message using the SI protocol
     */
    private fun writeMsg(command: Byte, data: ByteArray?, extended: Boolean): Int {
        val dataLen = data?.size ?: 0

        val size: Int = if (extended) {
            dataLen + 7
        } else {
            dataLen + 4
        }

        val buffer = ByteArray(size)
        buffer[0] = SIConstants.WAKEUP
        buffer[1] = SIConstants.STX
        buffer[2] = command

        if (extended) {
            buffer[3] = dataLen.toByte()
            data?.let { System.arraycopy(it, 0, buffer, 4, it.size) }
            val crc = calcSICrc(dataLen + 2, buffer.copyOfRange(2, buffer.size))
            buffer[dataLen + 4] = (crc and 0xff00 shr 8).toByte()
            buffer[dataLen + 5] = (crc and 0xff).toByte()
            buffer[dataLen + 6] = SIConstants.ETX
        } else {
            data?.let { System.arraycopy(it, 0, buffer, 3, it.size) }
            buffer[dataLen + 3] = SIConstants.ETX
        }

        val writtenBytes = port.syncWrite(buffer, SIConstants.WRITE_TIMEOUT)
        return if (writtenBytes == buffer.size) 0 else -1
    }

    private fun writeAck(): Int {
        val buffer = ByteArray(4)
        buffer[0] = 0xff.toByte()
        buffer[1] = SIConstants.STX
        buffer[2] = SIConstants.ACK
        buffer[3] = SIConstants.ETX

        val writtenBytes = port.syncWrite(buffer, SIConstants.WRITE_TIMEOUT)
        return if (writtenBytes == buffer.size) 0 else -1
    }

    fun writeNak(): Int {
        val buffer = ByteArray(4)
        buffer[0] = 0xff.toByte()
        buffer[1] = SIConstants.STX
        buffer[2] = SIConstants.NAK
        buffer[3] = SIConstants.ETX
        val writtenBytes = port.syncWrite(buffer, SIConstants.WRITE_TIMEOUT)
        return if (writtenBytes == buffer.size) 0 else -1
    }

    private fun enqueueCache(buffer: ByteArray) {
        msgCache.add(buffer)
    }


    private fun readMsg(timeout: Int): ByteArray? {
        return this.readMsg(timeout, SIConstants.ZERO)
    }

    private fun readMsg(timeout: Int, filter: Byte): ByteArray? {
        var bufferSize = 0
        var tmpBuffer: ByteArray? = dequeueCache(filter)
        var tmpBufferIndex = 0
        var bytesRead = 0
        var eof = false
        var dle = false

        //Check for cached message
        if (tmpBuffer != null) {
            return tmpBuffer
        }

        // Try to read all bytes from port
        val buffer = ByteArray(4096)
        tmpBuffer = ByteArray(4096)
        do {
            if (tmpBufferIndex >= bytesRead) {
                bytesRead = port.syncRead(tmpBuffer, timeout)
                if (bytesRead <= 0) {
                    break
                }
                tmpBufferIndex = 0
            }
            val incByte = tmpBuffer[tmpBufferIndex++]
            if (!(bufferSize == 0 && incByte == 0xff.toByte()) &&
                !(bufferSize == 0 && incByte == ZERO) &&
                !(bufferSize == 1 && incByte == SIConstants.STX)
            ) {
                buffer[bufferSize++] = incByte
            }

            // Check if we have received a NAK
            if (bufferSize == 1 && incByte == SIConstants.NAK) {
                eof = true
            }

            // If we have got to message type
            if (bufferSize > 1) {
                // If the command is in extended range
                if (byteToUnsignedInt(buffer[1]) > 0x80) {
                    if (bufferSize > 2 && bufferSize >= byteToUnsignedInt(buffer[2]) + 6) {
                        eof = true
                        // TODO check crc
                    }
                } else {
                    // If last char was a DLE, just continue
                    if (dle) {
                        dle = false
                    } else if (incByte == SIConstants.DLE) {
                        dle = true
                    } else if (incByte == SIConstants.ETX) {
                        eof = true
                    }
                }
            }

            // Check if message should be cached
            if (eof && filter != ZERO && bufferSize > 1 && filter != buffer[1]) {
                enqueueCache(buffer.copyOfRange(0, bufferSize))
                eof = false
                dle = false
                bufferSize = 0
            }
        } while (!eof)
        return if (eof && bufferSize > 0) {
            buffer.copyOfRange(0, bufferSize)
        } else {
            null
        }
    }

    private fun dequeueCache(filter: Byte): ByteArray? {
        for (i in msgCache.indices) {
            if (filter == ZERO || msgCache[i][1] == filter) {
                return msgCache.removeAt(i)
            }
        }
        return null
    }

    private fun byteToUnsignedInt(inByte: Byte): Int {
        return inByte.toInt() and 0xff
    }

    private fun probeDevice(): Boolean {
        var ret = false
        var msg: ByteArray
        var reply: ByteArray

        //Set the serial ports parameters
        port.syncOpen()
        port.setDataBits(UsbSerialInterface.DATA_BITS_8)
        port.setParity(UsbSerialInterface.PARITY_NONE)
        port.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)

        // Start with determining baud rate
        port.setBaudRate(SIConstants.BAUDRATE_HIGH)

        msg = byteArrayOf(0x4d)
        writeMsg(0xf0.toByte(), msg, true)
        reply = readMsg(1000, 0xf0.toByte())!!

        if (reply.isEmpty()) {
            Log.d("SI", "No response on high baud rate mode, trying low baud rate")
            port.setBaudRate(SIConstants.BAUDRATE_LOW)
        }

        writeMsg(0xf0.toByte(), msg, true)
        reply = readMsg(1000, 0xf0.toByte())!!

        if (reply.isNotEmpty()) {
            Log.d("SI", "Unit responded, reading device info")
            msg = byteArrayOf(ZERO, 0x75)
            writeMsg(SIConstants.GET_SYSTEM_INFO, msg, true)
            reply = readMsg(6000, 0x83.toByte())!!

            //Long info response
            if (reply.size >= 124) {
                Log.d("SI", "Got device info response")

                extendedMode =
                    (reply[122] and SIConstants.EXTENDED_MODE).compareTo(SIConstants.EXTENDED_MODE) == 0
                serialNo =
                    ((byteToUnsignedInt(reply[6]) shl 24) + (byteToUnsignedInt(reply[7]) shl 16)
                            + (byteToUnsignedInt(reply[8]) shl 8) + byteToUnsignedInt(reply[9]))
                ret = true
            }

            //Short info response
            else {
                Log.d("SI", "Invalid device info response, trying short info")

                msg = byteArrayOf(ZERO, 0x07)
                writeMsg(0x83.toByte(), msg, extendedMode)
                reply = readMsg(6000, 0x83.toByte())!!

                if (reply.size >= 10) {
                    Log.d("SI", "Got device info response")

                    extendedMode = false
                    serialNo =
                        ((byteToUnsignedInt(reply[6]) shl 24) + (byteToUnsignedInt(reply[7]) shl 16) + (byteToUnsignedInt(
                            reply[8]
                        ) shl 8) + byteToUnsignedInt(reply[9]))
                    ret = true
                }
            }
        }

        if (!ret) {
            port.syncClose()
        }
        return ret
    }

    /**
     * This function calculates the SportIdent CRC
     */
    private fun calcSICrc(uiCount: Int, pucDat: ByteArray): Int {
        var uiTmp1: Int
        var uiVal: Int
        var pucDatIndex = 0
        if (uiCount < 2) return 0
        uiTmp1 = pucDat[pucDatIndex++].toInt()
        uiTmp1 = (uiTmp1 shl 8) + pucDat[pucDatIndex++]
        if (uiCount == 2) return uiTmp1
        for (iTmp in uiCount shr 1 downTo 1) {
            if (iTmp > 1) {
                uiVal = pucDat[pucDatIndex++].toInt()
                uiVal = (uiVal shl 8) + pucDat[pucDatIndex++]
            } else {
                if (uiCount and 1 == 1) {
                    uiVal = pucDat[pucDatIndex].toInt()
                    uiVal = uiVal shl 8
                } else {
                    uiVal = 0
                }
            }
            for (uiTmp in 0..15) {
                if (uiTmp1 and 0x8000 == 0x8000) {
                    uiTmp1 = uiTmp1 shl 1
                    if (uiVal and 0x8000 == 0x8000) uiTmp1++
                    uiTmp1 = uiTmp1 xor SIConstants.POLYNOM
                } else {
                    uiTmp1 = uiTmp1 shl 1
                    if (uiVal and 0x8000 == 0x8000) uiTmp1++
                }
                uiVal = uiVal shl 1
            }
        }
        return uiTmp1 and 0xffff
    }

    private fun waitForCardInsert(cardData: CardData): Boolean {

        val reply: ByteArray? = readMsg(SIConstants.WRITE_TIMEOUT)
        if (reply != null && reply.isNotEmpty()) {
            when (reply[1]) {
                SIConstants.SI_CARD5, SIConstants.SI_CARD6, SIConstants.SI_CARD8_9_SIAC -> {
                    cardData.siNumber =
                        (byteToUnsignedInt(reply[6]) shl 16) + (byteToUnsignedInt(reply[7]) shl 8) + byteToUnsignedInt(
                            reply[8]
                        )
                    cardData.cardType = reply[1]
                    Log.d("SI", "Got card inserted event (CardID: " + cardData.siNumber + ")")
                    return true
                }

                SIConstants.SI_CARD_REMOVED -> {
                    val tmpCardId =
                        (byteToUnsignedInt(reply[5]) shl 24) + (byteToUnsignedInt(reply[6]) shl 16) + (byteToUnsignedInt(
                            reply[7]
                        ) shl 8) + byteToUnsignedInt(reply[8])
                    Log.d("SI", "Got card removed event (CardID: $tmpCardId)")
                }

                else -> Log.d("SI", "Got unknown command waiting for card inserted event")
            }
        }
        return false
    }

    /**
     * Attempts to read out the data from the SI card, based on the cardType
     */
    private fun readCardOnce() {
        val cardData = CardData(SIConstants.ZERO, 0, 0L, 0L, 0L, ArrayList())
        var valid = false

        if (waitForCardInsert(cardData)) {

            setStatusReading(cardData.siNumber)

            when (cardData.cardType) {
                SIConstants.SI_CARD5 -> valid = card5Readout(cardData)
                SIConstants.SI_CARD6 -> valid = card6Readout(cardData)
                SIConstants.SI_CARD8_9_SIAC -> valid = card89SiacReadout(cardData)
            }

            //If the readout was valid, process the data further
            if (valid) {
                //TODO: EMIT successful readout
                setStatusRead(cardData.siNumber)
                saveReadout(cardData)
            } else {
                setStatusRemoved(cardData.siNumber)
            }
        }
    }

    /**
     * Processes the read out punches, saving all into the database
     */
    private fun saveReadout(cardData: CardData) {
        //Check if another readout for the same card exist
        if (event != null) {
            val prev = dataProcessor.getReadoutBySINumber(cardData.siNumber, event!!.id)

            if (prev == null) {
                val readout =
                    Readout(
                        UUID.randomUUID(),
                        cardData.siNumber,
                        cardData.cardType,
                        event!!.id,
                        null, null, null, null,
                        Duration.ZERO,
                        LocalDateTime.now()
                    )

                for (pd in cardData.punchData) {
                    val punch = Punch(
                        UUID.randomUUID(),
                        event!!.id, null,
                        cardData.siNumber, pd.siCode,
                        pd.time,
                        PunchStatus.INVALID
                    )
                    dataProcessor.createPunch(punch)
                }

                //TODO: Save into database
            }

            //TODO: Info about existing readout

        }
    }

    private fun card5Readout(cardData: CardData): Boolean {

        writeMsg(SIConstants.GET_SI_CARD5, null, extendedMode)
        val reply: ByteArray? = readMsg(5000, SIConstants.GET_SI_CARD5)
        if (reply != null && card5EntryParse(reply, cardData)) {
            writeAck()
            return true
        }
        return false
    }

    private fun card5EntryParse(data: ByteArray, cardData: CardData): Boolean {
        var ret = false
        var offset = 0
        if (data.size == 136) {
            // Start at data part
            offset += 5
            // Get cardId
            if (data[offset + 6] == ZERO || data[offset + 6].toInt() == 0x01) {
                cardData.siNumber =
                    (byteToUnsignedInt(data[offset + 4]) shl 8) + byteToUnsignedInt(
                        data[offset + 5]
                    )
            } else if (byteToUnsignedInt(data[offset + 6]) < 5) {
                cardData.siNumber =
                    byteToUnsignedInt(data[offset + 6]) * 100000 + (byteToUnsignedInt(
                        data[offset + 4]
                    ) shl 8) + byteToUnsignedInt(data[offset + 5])

            } else {
                cardData.siNumber =
                    (byteToUnsignedInt(data[offset + 6]) shl 16) + (byteToUnsignedInt(
                        data[offset + 4]
                    ) shl 8) + byteToUnsignedInt(data[offset + 5])
            }
            cardData.startTime = ((byteToUnsignedInt(data[offset + 19]) shl 8) + byteToUnsignedInt(
                data[offset + 20]
            )).toLong()
            cardData.finishTime = ((byteToUnsignedInt(data[offset + 21]) shl 8) + byteToUnsignedInt(
                data[offset + 22]
            )).toLong()
            cardData.checkTime = ((byteToUnsignedInt(data[offset + 25]) shl 8) + byteToUnsignedInt(
                data[offset + 26]
            )).toLong()

            val punchCount = byteToUnsignedInt(data[offset + 23]) - 1
            run {
                var i = 0
                while (i < punchCount && i < 30) {
                    val punch = PunchData(0, 0L)
                    val baseOffset = offset + 32 + i / 5 * 16 + 1 + 3 * (i % 5)
                    punch.siCode = byteToUnsignedInt(data[baseOffset])
                    punch.time =
                        ((byteToUnsignedInt(data[baseOffset + 1]) shl 8) + byteToUnsignedInt(
                            data[baseOffset + 2]
                        )).toLong()
                    cardData.punchData.add(punch)
                    i++
                }
            }
            for (i in 30 until punchCount) {
                val punch = PunchData(0, 0L)
                val baseOffset = offset + 32 + (i - 30) * 16
                punch.siCode = data[baseOffset].toInt()
                punch.time = 0
                cardData.punchData.add(punch)
            }
            card5TimeAdjust(cardData)
            ret = true
        }
        return ret
    }

    /**
     * Adjust the times for the SI_CARD5, because it operates on 12h mode instead of 24h
     */
    private fun card5TimeAdjust(cardData: CardData) {
        val pmOffset: Long = if (zeroTimeBase >= HALF_DAY) HALF_DAY else 0
        if (cardData.startTime != 0L) {
            cardData.startTime = cardData.startTime * 1000 + pmOffset
            if (cardData.startTime < zeroTimeBase) {
                cardData.startTime += HALF_DAY
            }
            cardData.startTime -= zeroTimeBase
        }
        if (cardData.checkTime != 0L) {
            cardData.checkTime = cardData.checkTime * 1000 + pmOffset
            if (cardData.checkTime < zeroTimeBase) {
                cardData.checkTime += HALF_DAY
            }
            cardData.checkTime -= zeroTimeBase
        }
        var currentBase = pmOffset
        var lastTime: Long = zeroTimeBase
        for (punch in cardData.punchData) {
            val tmpTime: Long = punch.time * 1000 + currentBase
            //if (tmpTime < lastTime) {
            //    currentBase += HALF_DAY;
            //}
            //tmpTime = punch.time * 1000 + currentBase;
            punch.time = tmpTime - zeroTimeBase
            lastTime = tmpTime
        }
        var tmpTime: Long = cardData.finishTime * 1000 + currentBase
        if (tmpTime < lastTime) {
            currentBase += HALF_DAY
        }
        tmpTime = cardData.finishTime * 1000 + currentBase
        cardData.finishTime = tmpTime - zeroTimeBase
    }

    private fun card6Readout(cardData: CardData): Boolean {

        val reply = ByteArray(7 * 128)
        val msg = byteArrayOf(ZERO)
        val blocks = byteArrayOf(0, 6, 7, 2, 3, 4, 5)
        var i = 0
        while (i < 7) {

            msg[0] = blocks[i]
            writeMsg(SIConstants.GET_SI_CARD6, msg, extendedMode)
            val tmpReply: ByteArray? = readMsg(5000, SIConstants.GET_SI_CARD6)
            if (tmpReply == null || tmpReply.size != 128 + 6 + 3) {
                return false
            }
            System.arraycopy(tmpReply, 6, reply, i * 128, 128)
            if (i > 0) {
                if (tmpReply[124] == 0xee.toByte() && tmpReply[125] == 0xee.toByte() &&
                    tmpReply[126] == 0xee.toByte() && tmpReply[127] == 0xee.toByte()
                ) {
                    break   // Stop reading, no more punches
                }
            }
            i++
        }
        if (card6EntryParse(reply, cardData)) {
            writeAck()
            // EMIT card readout
            return true
        }
        // EMIT card read failed
        return false
    }

    private fun card6EntryParse(data: ByteArray, cardData: CardData): Boolean {
        cardData.siNumber =
            byteToUnsignedInt(data[10]) shl 24 or (byteToUnsignedInt(data[11]) shl 16) or (byteToUnsignedInt(
                data[12]
            ) shl 8) or byteToUnsignedInt(data[13])

        //Parse the special punches
        val startPunch = parsePunch(data.copyOfRange(24, 28))
        val finishPunch = parsePunch(data.copyOfRange(20, 24))
        val checkPunch = parsePunch(data.copyOfRange(28, 32))

        if (startPunch != null && finishPunch != null && checkPunch != null) {
            cardData.startTime = startPunch.time
            cardData.finishTime = finishPunch.time
            cardData.checkTime = checkPunch.time
        } else return false

        //Parse the regular punches
        val punches: Int = min(data[18].toInt(), 192)       //TODO: verify

        for (i in 0 until punches) {
            val tmpPunchData = parsePunch(data.copyOfRange(128 + 4 * i, 128 + 4 * i + 4))

            if (tmpPunchData != null) {
                cardData.punchData.add(tmpPunchData)
            } else {
                //Failed to parse a punchData
                return false
            }
        }
        return true
    }

    private fun card89SiacReadout(cardData: CardData): Boolean {

        //Request the first block with service data
        val msg = byteArrayOf(ZERO)
        val reply: ByteArray

        writeMsg(GET_SI_CARD8_9_SIAC, msg, extendedMode)
        var tmpReply: ByteArray? = readMsg(5000, GET_SI_CARD8_9_SIAC)

        if (tmpReply == null || tmpReply.size != 128 + 6 + 3) {
            return false
        }

        //Proceed with punch data blocks
        var nextBlock = 1
        var blockCount = 1
        val series = tmpReply[9].toInt() and SI_CARD10_11_SIAC_SERIES

        //Check if the card is SIAC - if so, adjust the blocks size
        if (series == SI_CARD10_11_SIAC_SERIES) {
            nextBlock = 4
            blockCount = 7       //(tmpReply[22] + 31) / 32
        }
        reply = ByteArray(128 * (1 + blockCount))
        System.arraycopy(tmpReply, 6, reply, 0, 128)

        var i = nextBlock

        //Read the punch data blocks from the device
        while (i <= blockCount) {
            msg[0] = i.toByte() //Request a block by number
            writeMsg(GET_SI_CARD8_9_SIAC, msg, extendedMode)
            tmpReply = readMsg(5000, GET_SI_CARD8_9_SIAC)

            if (tmpReply == null || tmpReply.size != 128 + 6 + 3) {
                // EMIT card read failed
                return false
            }
            System.arraycopy(tmpReply, 6, reply, (i - nextBlock + 1) * 128, 128)
            i++
        }

        //Parse the punchData and return status
        return if (card9EntryParse(reply, cardData)) {
            writeAck()
            true
        } else {
            false
        }
    }

    private fun card9EntryParse(data: ByteArray, cardData: CardData): Boolean {
        cardData.siNumber =
            byteToUnsignedInt(data[25]) shl 16 or (byteToUnsignedInt(data[26]) shl 8) or byteToUnsignedInt(
                data[27]
            )

        //Parse the special punches
        val series = data[24].toInt() and SI_CARD10_11_SIAC_SERIES
        val startPunch = parsePunch(data.copyOfRange(12, 16))
        val finishPunch = parsePunch(data.copyOfRange(16, 20))
        val checkPunch = parsePunch(data.copyOfRange(8, 12))

        if (startPunch != null && finishPunch != null && checkPunch != null) {
            cardData.startTime = startPunch.time
            cardData.finishTime = finishPunch.time
            cardData.checkTime = checkPunch.time
        } else return false

        when (series) {
            SI_CARD8_SERIES -> {
                //Determine number of punches by reading the pointer
                val punches: Int = min(
                    data[22].toInt(),
                    SI_CARD8_MAX_PUNCHES
                )

                //Go through all the punches and parse them
                for (i in 0 until punches) {
                    val tmpPunch = parsePunch(
                        data.copyOfRange(34 * 4 + 4 * i, 34 * 4 + 4 * i + 4)
                    )
                    if (tmpPunch != null) {
                        cardData.punchData.add(tmpPunch)
                    } else {
                        return false
                    }
                }
            }

            SI_CARD9_SERIES -> {
                //Determine number of punches by reading the pointer
                val punches: Int = min(
                    data[22].toInt(),
                    SI_CARD9_MAX_PUNCHES
                )

                //Go through all the punches and parse them
                for (i in 0 until punches) {
                    val tmpPunch = parsePunch(
                        data.copyOfRange(14 * 4 + 4 * i, 14 * 4 + 4 * i + 4)
                    )
                    if (tmpPunch != null) {
                        cardData.punchData.add(tmpPunch)
                    } else {
                        return false
                    }
                }
            }

            SI_CARD_PCARD_SERIES -> {
                //Determine number of punches by reading the pointer
                val punches: Int = min(
                    data[22].toInt(),
                    SI_CARD_PCARD_MAX_PUNCHES
                )

                //Go through all the punches and parse them
                for (i in 0 until punches) {
                    val tmpPunch = parsePunch(
                        data.copyOfRange(44 * 4 + 4 * i, 44 * 4 + 4 * i + 4)
                    )
                    if (tmpPunch != null) {
                        cardData.punchData.add(tmpPunch)
                    } else {
                        return false
                    }
                }
            }

            SI_CARD10_11_SIAC_SERIES -> {

                //Determine number of punches by reading the pointer
                val punches: Int = min(
                    data[22].toInt(),
                    SI_CARD10_11_SIAC_MAX_PUNCHES
                )

                //Go through all the punches and parse them
                for (i in 0 until punches) {
                    val tmpPunch: PunchData? =
                        parsePunch(data.copyOfRange(128 + 4 * i, 128 + 4 * i + 4))

                    if (tmpPunch != null) {
                        cardData.punchData.add(tmpPunch)
                    } else {
                        return false

                    }
                }
            }
        }
        return true
    }

    /**
     * Parses the given data into a PunchData object
     * @param data - The data read from the SI card
     * @return Parsed PunchData object, or null if invalid
     */
    private fun parsePunch(data: ByteArray): PunchData? {
        val punchData = PunchData(-1, -1L)
        if (data[0] == 0xee.toByte() && data[1] == 0xee.toByte() && data[2] == 0xee.toByte() && data[3] == 0xee.toByte()) {
            return null
        }
        punchData.siCode =
            byteToUnsignedInt(data[1]) + 256 * (byteToUnsignedInt(data[0]) shr 6 and 0x03)
        var basetime =
            ((byteToUnsignedInt(data[2]) shl 8 or byteToUnsignedInt(data[3])) * 1000).toLong()
        if (data[0].toInt() and 0x01 == 0x01) {
            basetime += HALF_DAY
        }
        var dayOfWeek = (data[0].toInt() shr 1 and 0x07).toLong()
        if (dayOfWeek < zeroTimeWeekDay) {
            dayOfWeek += 7
        }
        dayOfWeek -= zeroTimeWeekDay
        basetime += (dayOfWeek * 24 * 3600 * 1000)
        basetime -= zeroTimeBase
        punchData.time = basetime
        return punchData
    }

    private fun setStatusConnected() {
        readerStatus = SIReaderStatus.CONNECTED
        dataProcessor.siReaderState.postValue(
            SIReaderState(
                SIReaderStatus.CONNECTED,
                serialNo,
                null
            )
        )
    }

    private fun setStatusReading(cardNo: Int) {
        dataProcessor.siReaderState.postValue(
            SIReaderState(
                SIReaderStatus.READING,
                serialNo,
                cardNo
            )
        )
    }

    private fun setStatusRemoved(cardNo: Int) {
        readerStatus = SIReaderStatus.CONNECTED
        dataProcessor.siReaderState.postValue(
            SIReaderState(
                SIReaderStatus.CARD_REMOVED,
                serialNo,
                cardNo
            )
        )
    }

    private fun setStatusRead(cardNo: Int) {
        dataProcessor.siReaderState.postValue(
            SIReaderState(
                SIReaderStatus.CARD_READ,
                serialNo,
                cardNo
            )
        )
    }
}