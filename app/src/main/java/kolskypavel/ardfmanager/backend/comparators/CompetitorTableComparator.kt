package kolskypavel.ardfmanager.backend.comparators

import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData

class CompetitorNameComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o2.competitor?.let { o1.competitor?.lastName?.compareTo(it.lastName) } ?: 0
    }
}

class CompetitorStartNumComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o2.competitor?.let { o1.competitor?.startNumber?.compareTo(it.startNumber) } ?: 0
    }
}

class CompetitorClubComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o2.competitor?.let { o1.competitor?.club?.compareTo(it.club) } ?: 0
    }
}

class CompetitorCategoryComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData?, o2: CompetitorData?): Int {

        return compareBy<CompetitorData?> { it?.category?.name }
            .compare(o1, o2)
    }
}

class CompetitorSINumberComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {

        return o2.competitor?.siNumber?.let { o1.competitor?.siNumber?.compareTo(it) } ?: 0
    }
}
