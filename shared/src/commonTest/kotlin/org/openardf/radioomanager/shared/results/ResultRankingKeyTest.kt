package org.openardf.radioomanager.shared.results

import org.openardf.radioomanager.shared.domain.ResultStatus
import kotlin.test.Test
import kotlin.test.assertTrue

class ResultRankingKeyTest {
    @Test
    fun ranksLowerStatusOrdinalFirst() {
        assertTrue(
            ResultRankingKey(ResultStatus.OK, points = 0, runTimeNanos = 100) <
                ResultRankingKey(ResultStatus.MISPUNCHED, points = 0, runTimeNanos = 1)
        )
    }

    @Test
    fun ranksHigherPointsBeforeLowerPoints() {
        assertTrue(
            ResultRankingKey(ResultStatus.OK, points = 10, runTimeNanos = 100) <
                ResultRankingKey(ResultStatus.OK, points = 5, runTimeNanos = 1)
        )
    }

    @Test
    fun ranksShorterRunTimeBeforeLongerRunTime() {
        assertTrue(
            ResultRankingKey(ResultStatus.OK, points = 10, runTimeNanos = 100) <
                ResultRankingKey(ResultStatus.OK, points = 10, runTimeNanos = 200)
        )
    }
}
