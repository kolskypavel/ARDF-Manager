package kolskypavel.ardfmanager.backend.comparators

import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData

class CompetitorNameComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o2.competitorCategory.competitor.let {
            o1.competitorCategory.competitor.lastName.compareTo(
                it.lastName
            )
        } ?: 0
    }
}

class CompetitorStartNumComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o2.competitorCategory.competitor.let {
            o1.competitorCategory.competitor.startNumber.compareTo(
                it.startNumber
            )
        } ?: 0
    }
}

class CompetitorClubComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o2.competitorCategory.competitor.let {
            o1.competitorCategory.competitor.club.compareTo(
                it.club
            )
        } ?: 0
    }
}

class CompetitorCategoryComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData?, o2: CompetitorData?): Int {

        return compareBy<CompetitorData?> { it?.competitorCategory?.category?.name }
            .compare(o1, o2)
    }
}

class CompetitorSINumberComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {

        return o2.competitorCategory.competitor.siNumber?.let {
            o1.competitorCategory.competitor.siNumber?.compareTo(
                it
            )
        } ?: 0
    }
}
