package kolskypavel.ardfmanager.draw

import kolskypavel.ardfmanager.backend.draw.StartlistProcessor
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.ui.startlist.CategoryDrawWrapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class StartlistDrawTests {

    private fun createCompetitor(lastName: String, club: String): Competitor {
        return Competitor().apply {
            this.lastName = lastName
            this.club = club
            this.drawnRelativeStartTime = null
        }
    }

    private fun createCategoryWrapper(name: String, competitors: List<Competitor>, startPoint: Duration = Duration.ZERO): CategoryDrawWrapper {
        val cat = Category(name)
        val catData = CategoryData(cat, emptyList(), competitors)
        return CategoryDrawWrapper(catData, startPoint = startPoint)
    }

    @Test
    fun testDrawRandom() {
        val competitors = listOf(
            createCompetitor("A", "Club1"),
            createCompetitor("B", "Club1"),
            createCompetitor("C", "Club2")
        )
        val interval = Duration.ofMinutes(5)
        val wrapper = createCategoryWrapper("M21", competitors, startPoint = Duration.ofMinutes(10))

        StartlistProcessor.drawRandom(wrapper, interval)

        val times = competitors.mapNotNull { it.drawnRelativeStartTime }.sorted()
        assertEquals(3, times.size)
        assertEquals(Duration.ofMinutes(10), times[0])
        assertEquals(Duration.ofMinutes(15), times[1])
        assertEquals(Duration.ofMinutes(20), times[2])
    }

    @Test
    fun testDrawByClubSeparation() {
        // 2 clubs, 2 competitors each
        val competitors = listOf(
            createCompetitor("A1", "ClubA"),
            createCompetitor("A2", "ClubA"),
            createCompetitor("B1", "ClubB"),
            createCompetitor("B2", "ClubB")
        )
        val interval = Duration.ofMinutes(5)
        val wrapper = createCategoryWrapper("M21", competitors)

        StartlistProcessor.drawByClub(wrapper, interval)

        val sortedCompetitors = competitors.sortedBy { it.drawnRelativeStartTime }
        
        // With 2 clubs alternating, no two consecutive competitors should have the same club
        for (i in 0 until sortedCompetitors.size - 1) {
            assertTrue("Consecutive competitors ${sortedCompetitors[i].lastName} and ${sortedCompetitors[i+1].lastName} should have different clubs",
                sortedCompetitors[i].club != sortedCompetitors[i+1].club)
        }
    }

    @Test
    fun testDrawByClubSingleClub() {
        val competitors = listOf(
            createCompetitor("A1", "ClubA"),
            createCompetitor("A2", "ClubA")
        )
        val interval = Duration.ofMinutes(5)
        val wrapper = createCategoryWrapper("M21", competitors)

        StartlistProcessor.drawByClub(wrapper, interval)

        val times = competitors.mapNotNull { it.drawnRelativeStartTime }.sorted()
        assertEquals(2, times.size)
        assertEquals(Duration.ZERO, times[0])
        assertEquals(Duration.ofMinutes(5), times[1])
    }

    @Test
    fun testDrawStartTimesCoordinator() {
        val cat1Comps = listOf(createCompetitor("A", "C1"))
        val cat2Comps = listOf(createCompetitor("B", "C2"))
        
        val wrapper1 = createCategoryWrapper("C1", cat1Comps, startPoint = Duration.ZERO)
        val wrapper2 = createCategoryWrapper("C2", cat2Comps, startPoint = Duration.ofMinutes(30))
        
        val interval = Duration.ofMinutes(5)
        
        StartlistProcessor.drawStartTimes(listOf(wrapper1, wrapper2), interval, separateClubs = false)
        
        assertEquals(Duration.ZERO, cat1Comps[0].drawnRelativeStartTime)
        assertEquals(Duration.ofMinutes(30), cat2Comps[0].drawnRelativeStartTime)
    }

    @Test
    fun testDrawByClubUnevenClubs() {
        // ClubA has 3 members, ClubB has 1. 
        // Logic should alternate while possible: A, B, A, A or B, A, A, A
        val competitors = listOf(
            createCompetitor("A1", "ClubA"),
            createCompetitor("A2", "ClubA"),
            createCompetitor("A3", "ClubA"),
            createCompetitor("B1", "ClubB")
        )
        val interval = Duration.ofMinutes(5)
        val wrapper = createCategoryWrapper("M21", competitors)

        StartlistProcessor.drawByClub(wrapper, interval)

        val sorted = competitors.sortedBy { it.drawnRelativeStartTime }
        assertEquals(4, sorted.size)
        
        // One of the first two must be ClubB if we alternate
        assertTrue(sorted[0].club == "ClubB" || sorted[1].club == "ClubB")
    }
}
