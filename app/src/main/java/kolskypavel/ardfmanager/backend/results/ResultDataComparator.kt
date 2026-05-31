package kolskypavel.ardfmanager.backend.results

import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData

/** Sorts competitors so readouts appear before missing results, then by result ranking. */
class ResultDataComparator : Comparator<CompetitorData> {
    /** Compares null-readout competitors after competitors with readout data. */
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        val readoutData1 = o1.readoutData
        val readoutData2 = o2.readoutData

        if (readoutData1 == null && readoutData2 == null) {
            return 0
        } else if (readoutData1 == null) {
            return 1
        } else if (readoutData2 == null) {
            return -1
        }

        return readoutData1.result.compareTo(readoutData2.result)
    }
}
