package kolskypavel.ardfmanager.backend.results

import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData

class ResultDataComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        val readoutResult1 = o1.resultData
        val readoutResult2 = o2.resultData

        // Compare based on the existence of readoutResult
        if (readoutResult1 == null && readoutResult2 == null) {
            return 0
        } else if (readoutResult1 == null) {
            return 1
        } else if (readoutResult2 == null) {
            return -1
        }

        // Both readoutResult are not null, compare based on their result
        return readoutResult1.result.compareTo(readoutResult2.result)
    }
}