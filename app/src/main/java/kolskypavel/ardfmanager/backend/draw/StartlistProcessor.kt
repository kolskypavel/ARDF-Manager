package kolskypavel.ardfmanager.backend.draw

import kolskypavel.ardfmanager.ui.startlist.CategoryDrawWrapper
import java.time.Duration

object StartlistProcessor {

    fun drawStartTimes(
        categories: List<CategoryDrawWrapper>,
        startInterval: Duration,
        separateClubs: Boolean
    ) {
        for (cat in categories) {
            if (separateClubs) {
                drawByClub(cat, startInterval)
            } else drawRandom(cat, startInterval)
        }
    }

    fun drawRandom(
        category: CategoryDrawWrapper,
        startInterval: Duration
    ) {
        val randOrder = category.catData.competitors.shuffled()
        var currStart = category.startPoint

        for (comp in randOrder) {
            comp.drawnRelativeStartTime = currStart
            currStart += startInterval
        }
    }

    fun drawByClub(
        category: CategoryDrawWrapper,
        startInterval: Duration
    ) {
        val clubQueues = category.catData.competitors
            .groupBy { it.club }
            .values
            .map { it.toMutableList() }
            .shuffled()
            .toMutableList()

        var currStart = category.startPoint
        while (clubQueues.any { it.isNotEmpty() }) {
            for (queue in clubQueues) {
                if (queue.isNotEmpty()) {
                    val comp = queue.removeAt(0)
                    comp.drawnRelativeStartTime = currStart
                    currStart += startInterval
                }
            }
        }
    }
}