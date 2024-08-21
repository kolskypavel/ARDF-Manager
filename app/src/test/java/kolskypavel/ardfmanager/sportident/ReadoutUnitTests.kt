package kolskypavel.ardfmanager.sportident

import com.felhr.usbserial.UsbSerialDevice
import junit.framework.TestCase.assertEquals
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.sportident.SIPort
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SITime
import org.junit.Test
import org.mockito.Mockito.mock
import java.time.LocalTime

class ReadoutUnitTests {

    //Test the SI5 time adjustment because of the 12h format
    @Test
    fun testSI5DataAdjustment() {

        val checkTime = SITime(LocalTime.of(10, 5,0))
        val startTime = SITime(LocalTime.of(10, 10,0))

        val punchData = arrayListOf(
            SIPort.PunchData(1, SITime(LocalTime.of(10, 20, 0))),
            SIPort.PunchData(2, SITime(LocalTime.of(2, 21, 11))),
            SIPort.PunchData(3, SITime(LocalTime.of(3, 27, 25))),
            SIPort.PunchData(4, SITime(LocalTime.of(10, 20, 33))),
            SIPort.PunchData(5, SITime(LocalTime.of(4, 14, 44))),
            SIPort.PunchData(6, SITime(LocalTime.of(8, 14, 7))),
            SIPort.PunchData(6, SITime(LocalTime.of(8, 20, 24))),
            SIPort.PunchData(7, SITime(LocalTime.of(1, 33, 24))),
            SIPort.PunchData(8, SITime(LocalTime.of(3, 33, 2))),
            SIPort.PunchData(9, SITime(LocalTime.of(0, 0, 0))),
            SIPort.PunchData(10, SITime(LocalTime.of(9, 17, 10))),
        )

        val finishTime = SITime(LocalTime.of(9, 43,0))

        val zeroTimeBase = LocalTime.of(10, 0)
        val cardData = CardData(5, 12345, checkTime, startTime, finishTime, punchData)
        val mockDevice: UsbSerialDevice = mock()
        val mockDataProcessor: DataProcessor = mock()

        SIPort(mockDevice, mockDataProcessor).card5TimeAdjust(cardData, zeroTimeBase)
        assertEquals("10:05:00,0,0", cardData.checkTime.toString())
        assertEquals("10:10:00,0,0", cardData.startTime.toString())

        assertEquals("10:20:00,0,0", cardData.punchData[0].siTime.toString())
        assertEquals("14:21:11,0,0", cardData.punchData[1].siTime.toString())
        assertEquals("15:27:25,0,0", cardData.punchData[2].siTime.toString())
        assertEquals("22:20:33,0,0", cardData.punchData[3].siTime.toString())
        assertEquals("04:14:44,1,0", cardData.punchData[4].siTime.toString())
        assertEquals("08:14:07,1,0", cardData.punchData[5].siTime.toString())
        assertEquals("08:20:24,1,0", cardData.punchData[6].siTime.toString())
        assertEquals("13:33:24,1,0", cardData.punchData[7].siTime.toString())
        assertEquals("15:33:02,1,0", cardData.punchData[8].siTime.toString())
        assertEquals("00:00:00,2,0", cardData.punchData[9].siTime.toString())
        assertEquals("09:17:10,2,0", cardData.punchData[10].siTime.toString())

        assertEquals("09:43:00,2,0", cardData.finishTime.toString())
    }
}