package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ResultStatus

/** Comparable ranking key for sorting results by status, points, and runtime. */
data class ResultRankingKey(
    val status: ResultStatus,
    val points: Int,
    val runTimeNanos: Long
) : Comparable<ResultRankingKey> {
    override fun compareTo(other: ResultRankingKey): Int {
        return when {
            status != other.status -> status.compareTo(other.status)
            points != other.points -> other.points.compareTo(points)
            else -> runTimeNanos.compareTo(other.runTimeNanos)
        }
    }
}
