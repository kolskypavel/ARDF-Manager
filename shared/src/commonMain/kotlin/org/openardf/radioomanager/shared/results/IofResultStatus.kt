package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ResultStatus

/** Maps internal result statuses to IOF XML status strings. */
object IofResultStatus {
    /** Returns the IOF status value for a shared result status. */
    fun fromResultStatus(resultStatus: ResultStatus): String {
        return when (resultStatus) {
            ResultStatus.OK -> "OK"
            ResultStatus.MISPUNCHED -> "MissingPunch"
            ResultStatus.NO_RANKING -> "MissingPunch"
            ResultStatus.DISQUALIFIED -> "Disqualified"
            ResultStatus.DID_NOT_START -> "DidNotStart"
            ResultStatus.DID_NOT_FINISH -> "DidNotFinish"
            ResultStatus.OVER_TIME_LIMIT -> "OverTime"
            ResultStatus.UNOFFICIAL -> "NotCompeting"
            ResultStatus.ERROR -> "Cancelled"
        }
    }
}
