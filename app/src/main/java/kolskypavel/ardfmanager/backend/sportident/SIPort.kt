package kolskypavel.ardfmanager.backend.sportident

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.util.Arrays
import kotlin.experimental.and

class SIPort(private val port: UsbSerialDevice, context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val msgCache: ArrayList<ByteArray> = ArrayList()
    public var extendedMode = false
    public var serialNo: Long = 0L

    fun writeMsg(command: Byte, data: ByteArray?, extended: Boolean): Int {
        var datalen = 0
        datalen = data?.size ?: 0

        val size: Int = if (extended) {
            datalen + 7
        } else {
            datalen + 4
        }

        val buffer = ByteArray(size)
        buffer[0] = SIConstants.WAKEUP
        buffer[1] = SIConstants.STX
        buffer[2] = command
        if (extended) {
            buffer[3] = datalen.toByte()
            data?.let { System.arraycopy(it, 0, buffer, 4, it.size) }
            val crc = calcSICrc(datalen + 2, buffer.copyOfRange(2, buffer.size))
            buffer[datalen + 4] = (crc and 0xff00 shr 8).toByte()
            buffer[datalen + 5] = (crc and 0xff).toByte()
            buffer[datalen + 6] = SIConstants.ETX
        } else {
            data?.let { System.arraycopy(it, 0, buffer, 3, it.size) }
            buffer[datalen + 3] = SIConstants.ETX
        }

        val writtenBytes = port.syncWrite(buffer, SIConstants.WRITE_TIMEOUT)
        return if (writtenBytes == buffer.size) 0 else -1
    }

    fun writeAck(): Int {
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


    fun readMsg(timeout: Int): ByteArray? {
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
                if (bytesRead == 0) {
                    break
                }
                tmpBufferIndex = 0
            }
            val incByte = tmpBuffer[tmpBufferIndex++]
            if (!(bufferSize == 0 && incByte == 0xff.toByte()) &&
                !(bufferSize == 0 && incByte.toInt() == 0x00) &&
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
            if (eof && filter.toInt() != 0x00 && bufferSize > 1 && filter != buffer[1]) {
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
            if (filter.toInt() == 0x00 || msgCache[i][1] == filter) {
                return msgCache.removeAt(i)
            }
        }
        return null
    }

    private fun byteToUnsignedInt(inByte: Byte): Int {
        return inByte.toInt() and 0xff
    }

    fun probeDevice(): Boolean {
        var ret = false
        var msg: ByteArray
        var reply: ByteArray

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
            msg = byteArrayOf(0x00, 0x75)
            writeMsg(SIConstants.GET_SYSTEM_INFO, msg, true)
            reply = readMsg(6000, 0x83.toByte())!!

            //Long info response
            if (reply.size >= 124) {
                Log.d("SI", "Got device info response")

                extendedMode =
                    (reply[122] and SIConstants.EXTENDED_MODE).compareTo(SIConstants.EXTENDED_MODE) == 0
                serialNo =
                    ((byteToUnsignedInt(reply[6]) shl 24) + (byteToUnsignedInt(reply[7]) shl 16) + (byteToUnsignedInt(
                        reply[8]
                    ) shl 8) + byteToUnsignedInt(reply[9])).toLong()
                ret = true
            }
            //Short info response
            else {
                Log.d("SI", "Invalid device info response, trying short info")

                msg = byteArrayOf(0x00, 0x07)
                writeMsg(0x83.toByte(), msg, true)
                reply = readMsg(6000, 0x83.toByte())!!

                if (reply.size >= 10) {
                    Log.d("SI", "Got device info response")

                    extendedMode = false
                    serialNo =
                        ((byteToUnsignedInt(reply[6]) shl 24) + (byteToUnsignedInt(reply[7]) shl 16) + (byteToUnsignedInt(
                            reply[8]
                        ) shl 8) + byteToUnsignedInt(reply[9])).toLong()
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

    fun waitForCardInsert(timeout: Int): Boolean {

        val reply: ByteArray? = readMsg(timeout)
        if (reply != null && reply.isNotEmpty()) {
            when (reply[1]) {
                0xe5.toByte(), 0xe6.toByte(), 0xe8.toByte() -> {
                    cardInfo.cardId =
                        (byteToUnsignedInt(reply[6]) shl 16) + (byteToUnsignedInt(reply[7]) shl 8) + byteToUnsignedInt(
                            reply[8]
                        )
                    cardInfo.format = reply[1]
                    Log.d(TAG, "Got card inserted event (CardID: " + cardInfo.cardId + ")")
                    return true
                }

                0xe7.toByte() -> {
                    val tmpCardId =
                        (byteToUnsignedInt(reply[5]) shl 24) + (byteToUnsignedInt(reply[6]) shl 16) + (byteToUnsignedInt(
                            reply[7]
                        ) shl 8) + byteToUnsignedInt(reply[8])
                    Log.d(TAG, "Got card removed event (CardID: $tmpCardId)")
                }

                else -> Log.d(TAG, "Got unknown command waiting for card inserted event")
            }
        }
        return false
    }


    private fun readCardOnce() {
        val entry: CardEntry
        val msg: ByteArray
        var reply: ByteArray?
        val proto: SIProtocol = siReader.getProtoObj()
        val cardInfo: SIReader.SiCardInfo = SiCardInfo()
        if (siReader.waitForCardInsert(500, cardInfo)) {
            when (cardInfo.format) {
                0xe5.toByte() -> {
                    entry = CardEntry()

                    // EMIT card reading
                    this.emitReadStarted(cardInfo)
                    proto.writeMsg(0xb1.toByte(), null, true)
                    reply = proto.readMsg(5000, 0xb1.toByte())
                    if (reply != null && card5EntryParse(reply, entry)) {
                        proto.writeAck()
                        // EMIT card read out
                        this.emitReadout(entry)
                    } else {
                        // EMIT card read failed
                        this.emitReadCanceled()
                    }
                }

                0xe6.toByte() -> {
                    entry = CardEntry()
                    reply = ByteArray(7 * 128)

                    // EMIT card reading
                    this.emitReadStarted(cardInfo)
                    msg = byteArrayOf(0x00)
                    val blocks = byteArrayOf(0, 6, 7, 2, 3, 4, 5)
                    var i = 0
                    while (i < 7) {
                        msg[0] = blocks[i]
                        proto.writeMsg(0xe1.toByte(), msg, true)
                        val tmpReply: ByteArray = proto.readMsg(5000, 0xe1.toByte())
                        if (tmpReply == null || tmpReply.size != 128 + 6 + 3) {
                            // EMIT card read failed
                            reply = null
                            this.emitReadCanceled()
                            break
                        }
                        System.arraycopy(tmpReply, 6, reply, i * 128, 128)
                        if (i > 0) {
                            if (tmpReply[124] == 0xee.toByte() && tmpReply[125] == 0xee.toByte() && tmpReply[126] == 0xee.toByte() && tmpReply[127] == 0xee.toByte()) {
                                // Stop reading, no more punches
                                break
                            }
                        }
                        i++
                    }
                    if (reply != null && card6EntryParse(reply, entry)) {
                        proto.writeAck()
                        // EMIT card readout
                        this.emitReadout(entry)
                    } else {
                        // EMIT card read failed
                        this.emitReadCanceled()
                    }
                }

                0xe8.toByte() -> {
                    entry = CardEntry()

                    // EMIT card reading
                    this.emitReadStarted(cardInfo)
                    msg = byteArrayOf(0x00)
                    proto.writeMsg(0xef.toByte(), msg, true)
                    var tmpReply: ByteArray = proto.readMsg(5000, 0xef.toByte())
                    if (tmpReply == null || tmpReply.size != 128 + 6 + 3) {
                        // EMIT card read failed
                        this.emitReadCanceled()
                        break
                    }
                    val series = tmpReply[24].toInt() and 0x0f
                    var nextBlock = 1
                    var blockCount = 1
                    if (series == 0x0f) {
                        // siac
                        nextBlock = 4
                        blockCount = (tmpReply[22] + 31) / 32
                    }
                    reply = ByteArray(128 * (1 + blockCount))
                    System.arraycopy(tmpReply, 6, reply, 0, 128)
                    var i = nextBlock
                    while (i < nextBlock + blockCount) {
                        msg[0] = i.toByte()
                        proto.writeMsg(0xef.toByte(), msg, true)
                        tmpReply = proto.readMsg(5000, 0xef.toByte())
                        if (tmpReply == null || tmpReply.size != 128 + 6 + 3) {
                            // EMIT card read failed
                            reply = null
                            this.emitReadCanceled()
                            break
                        }
                        System.arraycopy(tmpReply, 6, reply, (i - nextBlock + 1) * 128, 128)
                        i++
                    }
                    if (reply != null && card9EntryParse(reply, entry)) {
                        proto.writeAck()
                        // EMIT card read out
                        this.emitReadout(entry)
                    } else {
                        // EMIT card read failed
                        this.emitReadCanceled()
                    }
                }

                else -> {}
            }
        }
    }

    private fun card5EntryParse(data: ByteArray?, entry: CardEntry): Boolean {
        var ret = false
        var offset = 0
        if (data!!.size == 136) {
            // Start at data part
            offset += 5
            // Get cardId
            if (data[offset + 6].toInt() == 0x00 || data[offset + 6].toInt() == 0x01) {
                entry.cardId = (byteToUnsignedInt(data[offset + 4]) shl 8) + byteToUnsignedInt(
                    data[offset + 5]
                )
            } else if (byteToUnsignedInt(data[offset + 6]) < 5) {
                entry.cardId = byteToUnsignedInt(data[offset + 6]) * 100000 + (byteToUnsignedInt(
                    data[offset + 4]
                ) shl 8) + byteToUnsignedInt(data[offset + 5])
            } else {
                entry.cardId = (byteToUnsignedInt(data[offset + 6]) shl 16) + (byteToUnsignedInt(
                    data[offset + 4]
                ) shl 8) + byteToUnsignedInt(data[offset + 5])
            }
            entry.startTime = (byteToUnsignedInt(data[offset + 19]) shl 8) + byteToUnsignedInt(
                data[offset + 20]
            )
            entry.finishTime = (byteToUnsignedInt(data[offset + 21]) shl 8) + byteToUnsignedInt(
                data[offset + 22]
            )
            entry.checkTime = (byteToUnsignedInt(data[offset + 25]) shl 8) + byteToUnsignedInt(
                data[offset + 26]
            )
            val punchCount = byteToUnsignedInt(data[offset + 23]) - 1
            run {
                var i = 0
                while (i < punchCount && i < 30) {
                    val punch = Punch()
                    val baseoffset = offset + 32 + i / 5 * 16 + 1 + 3 * (i % 5)
                    punch.code = byteToUnsignedInt(data[baseoffset])
                    punch.time =
                        (byteToUnsignedInt(data[baseoffset + 1]) shl 8) + byteToUnsignedInt(
                            data[baseoffset + 2]
                        )
                    entry.punches.add(punch)
                    i++
                }
            }
            for (i in 30 until punchCount) {
                val punch = Punch()
                val baseoffset = offset + 32 + (i - 30) * 16
                punch.code = data[baseoffset]
                punch.time = 0
                entry.punches.add(punch)
            }
            card5TimeAdjust(entry)
            ret = true
        }
        return ret
    }

    private fun card6EntryParse(data: ByteArray, entry: CardEntry): Boolean {
        entry.cardId =
            byteToUnsignedInt(data[10]) shl 24 or (byteToUnsignedInt(data[11]) shl 16) or (byteToUnsignedInt(
                data[12]
            ) shl 8) or byteToUnsignedInt(data[13])
        val startPunch = Punch()
        val finishPunch = Punch()
        val checkPunch = Punch()
        parsePunch(Arrays.copyOfRange(data, 24, 28), startPunch)
        parsePunch(Arrays.copyOfRange(data, 20, 24), finishPunch)
        parsePunch(Arrays.copyOfRange(data, 28, 32), checkPunch)
        entry.startTime = startPunch.time
        entry.finishTime = finishPunch.time
        entry.checkTime = checkPunch.time
        val punches: Int = min(data[18], 192)
        for (i in 0 until punches) {
            val tmpPunch = Punch()
            if (parsePunch(Arrays.copyOfRange(data, 128 + 4 * i, 128 + 4 * i + 4), tmpPunch)) {
                entry.punches.add(tmpPunch)
            }
        }
        return true
    }

    private fun card9EntryParse(data: ByteArray, entry: CardEntry): Boolean {
        entry.cardId =
            byteToUnsignedInt(data[25]) shl 16 or (byteToUnsignedInt(data[26]) shl 8) or byteToUnsignedInt(
                data[27]
            )
        val series = data[24].toInt() and 0x0f
        val startPunch = Punch()
        val finishPunch = Punch()
        val checkPunch = Punch()
        parsePunch(Arrays.copyOfRange(data, 12, 16), startPunch)
        parsePunch(Arrays.copyOfRange(data, 16, 20), finishPunch)
        parsePunch(Arrays.copyOfRange(data, 8, 12), checkPunch)
        entry.startTime = startPunch.time
        entry.finishTime = finishPunch.time
        entry.checkTime = checkPunch.time
        if (series == 1) {
            // SI card 9
            val punches: Int = min(data[22], 50)
            for (i in 0 until punches) {
                val tmpPunch = Punch()
                if (parsePunch(
                        Arrays.copyOfRange(data, 14 * 4 + 4 * i, 14 * 4 + 4 * i + 4),
                        tmpPunch
                    )
                ) {
                    entry.punches.add(tmpPunch)
                }
            }
        } else if (series == 2) {
            // SI card 8
            val punches: Int = min(data[22], 30)
            for (i in 0 until punches) {
                val tmpPunch = Punch()
                if (parsePunch(
                        Arrays.copyOfRange(data, 34 * 4 + 4 * i, 34 * 4 + 4 * i + 4),
                        tmpPunch
                    )
                ) {
                    entry.punches.add(tmpPunch)
                }
            }
        } else if (series == 4) {
            // pCard
            val punches: Int = min(data[22], 20)
            for (i in 0 until punches) {
                val tmpPunch = Punch()
                if (parsePunch(
                        Arrays.copyOfRange(data, 44 * 4 + 4 * i, 44 * 4 + 4 * i + 4),
                        tmpPunch
                    )
                ) {
                    entry.punches.add(tmpPunch)
                }
            }
        } else if (series == 15) {
            // SI card 10, 11, siac
            val punches: Int = min(data[22], 128)
            for (i in 0 until punches) {
                val tmpPunch = Punch()
                if (parsePunch(Arrays.copyOfRange(data, 128 + 4 * i, 128 + 4 * i + 4), tmpPunch)) {
                    entry.punches.add(tmpPunch)
                }
            }
        }
        return true
    }

    private fun card5TimeAdjust(entry: CardEntry) {
        val pmOffset: Long = if (zeroTimeBase >= HALF_DAY) HALF_DAY else 0
        if (entry.startTime !== 0) {
            entry.startTime = entry.startTime * 1000 + pmOffset
            if (entry.startTime < zeroTimeBase) {
                entry.startTime += HALF_DAY
            }
            entry.startTime -= zeroTimeBase
        }
        if (entry.checkTime !== 0) {
            entry.checkTime = entry.checkTime * 1000 + pmOffset
            if (entry.checkTime < zeroTimeBase) {
                entry.checkTime += HALF_DAY
            }
            entry.checkTime -= zeroTimeBase
        }
        var currentBase = pmOffset
        var lastTime: Long = zeroTimeBase
        for (punch in entry.punches) {
            val tmpTime: Long = punch.time * 1000 + currentBase
            //if (tmpTime < lastTime) {
            //    currentBase += HALF_DAY;
            //}
            //tmpTime = punch.time * 1000 + currentBase;
            punch.time = tmpTime - zeroTimeBase
            lastTime = tmpTime
        }
        var tmpTime: Long = entry.finishTime * 1000 + currentBase
        if (tmpTime < lastTime) {
            currentBase += HALF_DAY
        }
        tmpTime = entry.finishTime * 1000 + currentBase
        entry.finishTime = tmpTime - zeroTimeBase
    }

    private fun card5TimeAdjust(entry: CardEntry) {
        val pmOffset: Long = if (zeroTimeBase >= HALF_DAY) HALF_DAY else 0
        if (entry.startTime !== 0) {
            entry.startTime = entry.startTime * 1000 + pmOffset
            if (entry.startTime < zeroTimeBase) {
                entry.startTime += HALF_DAY
            }
            entry.startTime -= zeroTimeBase
        }
        if (entry.checkTime !== 0) {
            entry.checkTime = entry.checkTime * 1000 + pmOffset
            if (entry.checkTime < zeroTimeBase) {
                entry.checkTime += HALF_DAY
            }
            entry.checkTime -= zeroTimeBase
        }
        var currentBase = pmOffset
        var lastTime: Long = zeroTimeBase
        for (punch in entry.punches) {
            val tmpTime: Long = punch.time * 1000 + currentBase
            //if (tmpTime < lastTime) {
            //    currentBase += HALF_DAY;
            //}
            //tmpTime = punch.time * 1000 + currentBase;
            punch.time = tmpTime - zeroTimeBase
            lastTime = tmpTime
        }
        var tmpTime: Long = entry.finishTime * 1000 + currentBase
        if (tmpTime < lastTime) {
            currentBase += HALF_DAY
        }
        tmpTime = entry.finishTime * 1000 + currentBase
        entry.finishTime = tmpTime - zeroTimeBase
    }

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        return
    }
}