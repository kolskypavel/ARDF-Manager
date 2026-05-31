package org.openardf.radioomanager.shared.event

import org.openardf.radioomanager.shared.domain.StandardCategoryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StandardCategoryRulesTest {
    @Test
    fun parsesStandardCategoryDefinitions() {
        assertEquals(
            StandardCategoryDefinition(name = "M21", isMan = true, maxAge = 39),
            StandardCategoryRules.parseDefinition(" M21 ; 1 ; 39 ")
        )
        assertEquals(
            StandardCategoryDefinition(name = "W21", isMan = false, maxAge = 34),
            StandardCategoryRules.parseDefinition("W21;0;34")
        )
    }

    @Test
    fun rejectsInvalidStandardCategoryDefinitions() {
        assertNull(StandardCategoryRules.parseDefinition("M21;1"))
        assertNull(StandardCategoryRules.parseDefinition(";1;39"))
        assertNull(StandardCategoryRules.parseDefinition("M21;true;39"))
        assertNull(StandardCategoryRules.parseDefinition("M21;1;0"))
        assertNull(StandardCategoryRules.parseDefinition("M21;1;age"))
    }

    @Test
    fun providesBuiltInStandardCategoryDefinitions() {
        val international = StandardCategoryRules.definitionsFor(StandardCategoryType.INTERNATIONAL)
        val czech = StandardCategoryRules.definitionsFor(StandardCategoryType.CZECH)

        assertEquals(12, international.size)
        assertEquals(StandardCategoryDefinition(name = "W19", isMan = false, maxAge = 19), international.first())
        assertEquals(StandardCategoryDefinition(name = "M70", isMan = true, maxAge = 200), international.last())
        assertEquals(22, czech.size)
        assertEquals(StandardCategoryDefinition(name = "D7", isMan = false, maxAge = 7), czech.first())
        assertEquals(StandardCategoryDefinition(name = "M70", isMan = true, maxAge = 200), czech.last())
    }
}
