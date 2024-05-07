package kolskypavel.ardfmanager.backend.results

import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData

class ResultDataComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o2.readoutResult?.let { o1.readoutResult?.result?.compareTo(it.result) } ?: 0
    }
}