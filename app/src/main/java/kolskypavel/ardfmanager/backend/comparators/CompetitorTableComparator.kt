package kolskypavel.ardfmanager.backend.comparators

import kolskypavel.ardfmanager.backend.room.entitity.CompetitorCategory

class CompetitorFirstNameComparator : Comparator<CompetitorCategory> {
    override fun compare(o1: CompetitorCategory, o2: CompetitorCategory): Int {
        return o1.competitor.firstName.compareTo(o2.competitor.firstName)
    }
}

class CompetitorLastNameComparator : Comparator<CompetitorCategory> {
    override fun compare(o1: CompetitorCategory, o2: CompetitorCategory): Int {
        return o1.competitor.lastName.compareTo(o2.competitor.lastName)
    }
}

class CompetitorClubComparator : Comparator<CompetitorCategory> {
    override fun compare(o1: CompetitorCategory, o2: CompetitorCategory): Int {
        return o1.competitor.club.compareTo(o2.competitor.club)
    }
}

class CompetitorCategoryComparator : Comparator<CompetitorCategory> {
    override fun compare(o1: CompetitorCategory?, o2: CompetitorCategory?): Int {

        return compareBy<CompetitorCategory?> { it?.category?.name }
            .compare(o1, o2)
    }
}
