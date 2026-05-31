package org.openardf.radioomanager.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class DomainEnumTest {
    @Test
    fun resolvesRaceMetadataEnumsByStoredValue() {
        assertEquals(RaceType.CLASSIC, RaceType.getByValue(-1))
        assertEquals(RaceType.SPRINT, RaceType.getByValue(2))
        assertEquals(RaceLevel.PRACTICE, RaceLevel.getByValue(-1))
        assertEquals(RaceLevel.DISTRICT, RaceLevel.getByValue(3))
        assertEquals(RaceBand.M80, RaceBand.getByValue(-1))
        assertEquals(RaceBand.COMBINED, RaceBand.getByValue(2))
    }

    @Test
    fun resolvesResultAndPunchEnumsByStoredValue() {
        assertEquals(ResultStatus.NO_RANKING, ResultStatus.getByValue(-1))
        assertEquals(ResultStatus.OVER_TIME_LIMIT, ResultStatus.getByValue(6))
        assertEquals(PunchStatus.VALID, PunchStatus.getByValue(-1))
        assertEquals(PunchStatus.DUPLICATE, PunchStatus.getByValue(2))
    }

    @Test
    fun resolvesControlProviderAndCategoryEnumsByStoredValue() {
        assertEquals(ControlPointType.CONTROL, ControlPointType.getByValue(-1))
        assertEquals(ControlPointType.BEACON, ControlPointType.getByValue(1))
        assertEquals(ProviderType.ROBIS, ProviderType.getByValue(-1))
        assertEquals(ProviderType.OFEED, ProviderType.getByValue(3))
        assertEquals(StandardCategoryType.INTERNATIONAL, StandardCategoryType.getByValue(-1))
        assertEquals(StandardCategoryType.CZECH, StandardCategoryType.getByValue(1))
    }
}
