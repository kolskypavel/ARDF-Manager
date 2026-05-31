package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ResultStatus

/** Comparable ranking key for sorting results by status, points, and runtime. */
data class ResultRankingKey(
    /** Result status priority. */
    val status: ResultStatus,
    /** Score used for ARDF-style ranking, sorted descending. */
    val points: Int,
    /** Runtime used as the final tie-breaker, sorted ascending. */
    val runTimeNanos: Long
) : Comparable<ResultRankingKey> {
    /** Orders by status, then higher points, then lower runtime. */
    override fun compareTo(other: ResultRankingKey): Int {
        return when {
            status != other.status -> status.compareTo(other.status)
            points != other.points -> other.points.compareTo(points)
            else -> runTimeNanos.compareTo(other.runTimeNanos)
        }
    }
}
