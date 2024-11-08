package kolskypavel.ardfmanager.ui.competitors

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

class CompetitorSINumberComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {

        val si1 = o1.competitorCategory.competitor.siNumber
        val si2 = o2.competitorCategory.competitor.siNumber

        return when {
            si1 == null && si2 == null -> 0            // Both null, consider them equal
            si1 == null -> 1                            // si1 is null, place it after si2
            si2 == null -> -1                           // si2 is null, place it after si1
            else -> si1.compareTo(si2)                  // Both are non-null, compare by value
        }
    }
}

class CompetitorStartTimeComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {

        return o1.competitorCategory.competitor.drawnRelativeStartTime?.compareTo(o2.competitorCategory.competitor.drawnRelativeStartTime)
            ?: -1
    }
}

class CompetitorFinishTimeComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o1.resultData?.result?.finishTime?.compareTo(o2.resultData?.result?.finishTime) ?: -1
    }
}

class CompetitorRunTimeComparator : Comparator<CompetitorData> {
    override fun compare(o1: CompetitorData, o2: CompetitorData): Int {
        return o1.resultData?.result?.runTime?.compareTo(o2.resultData?.result?.runTime) ?: -1
    }
}


