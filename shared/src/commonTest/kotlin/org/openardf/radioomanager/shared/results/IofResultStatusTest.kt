package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ResultStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class IofResultStatusTest {
    @Test
    fun mapsRadioOManagerStatusesToIofStatuses() {
        assertEquals("OK", IofResultStatus.fromResultStatus(ResultStatus.OK))
        assertEquals("MissingPunch", IofResultStatus.fromResultStatus(ResultStatus.MISPUNCHED))
        assertEquals("MissingPunch", IofResultStatus.fromResultStatus(ResultStatus.NO_RANKING))
        assertEquals("Disqualified", IofResultStatus.fromResultStatus(ResultStatus.DISQUALIFIED))
        assertEquals("DidNotStart", IofResultStatus.fromResultStatus(ResultStatus.DID_NOT_START))
        assertEquals("DidNotFinish", IofResultStatus.fromResultStatus(ResultStatus.DID_NOT_FINISH))
        assertEquals("OverTime", IofResultStatus.fromResultStatus(ResultStatus.OVER_TIME_LIMIT))
        assertEquals("NotCompeting", IofResultStatus.fromResultStatus(ResultStatus.UNOFFICIAL))
        assertEquals("Cancelled", IofResultStatus.fromResultStatus(ResultStatus.ERROR))
    }
}
