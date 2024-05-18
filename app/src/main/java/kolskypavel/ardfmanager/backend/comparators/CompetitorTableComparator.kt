package kolskypavel.ardfmanager.backend.comparators

import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData

class CompetitorNameComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o1.competitorCategory.competitor.lastName.compareTo(
            o2.competitorCategory.competitor.lastName,
            true
        )
    }
}


class CompetitorStartNumComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o1.competitorCategory.competitor.startNumber.compareTo(o2.competitorCategory.competitor.startNumber)
    }
}

class CompetitorClubComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o1.competitorCategory.competitor.club.compareTo(o2.competitorCategory.competitor.club)
    }
}

class CompetitorCategoryComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {

        return o2.competitorCategory.category?.let {
            o1.competitorCategory.category?.name?.compareTo(
                it.name
            )
        }
            ?: 0
    }
}

class CompetitorStartTimeComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {

        return o1.competitorCategory.competitor.drawnRelativeStartTime?.compareTo(o2.competitorCategory.competitor.drawnRelativeStartTime)
            ?: -1
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
