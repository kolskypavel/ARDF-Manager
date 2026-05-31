package org.openardf.radioomanager.shared.device

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SIReaderStateTest {
    @Test
    fun storesReaderStatusAndOptionalCardMetadata() {
        val disconnected = SIReaderState(SIReaderStatus.DISCONNECTED)

        assertEquals(SIReaderStatus.DISCONNECTED, disconnected.status)
        assertNull(disconnected.stationId)
        assertNull(disconnected.cardId)
        assertNull(disconnected.lastCard)

        val reading = SIReaderState(
            status = SIReaderStatus.READING,
            stationId = 31,
            cardId = 123456,
            lastCard = 123455
        )

        assertEquals(SIReaderStatus.READING, reading.status)
        assertEquals(31, reading.stationId)
        assertEquals(123456, reading.cardId)
        assertEquals(123455, reading.lastCard)
    }
}
