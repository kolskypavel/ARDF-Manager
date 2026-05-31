package org.openardf.radioomanager.shared.domain

enum class ResultStatus(val value: Int) : Comparable<ResultStatus> {
    OK(0),
    MISPUNCHED(1),
    NO_RANKING(2),
    DISQUALIFIED(3),
    DID_NOT_START(4),
    DID_NOT_FINISH(5),
    OVER_TIME_LIMIT(6),
    UNOFFICIAL(7),
    ERROR(8);

    companion object {
        fun getByValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: NO_RANKING
    }
}
