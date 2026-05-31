package org.openardf.radioomanager.shared.course

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.RaceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ControlPointRulesTest {
    @Test
    fun parsesAndFormatsControlPointStrings() {
        val controlPoints = ControlPointRules.parseControlPoints("31 32 36B", RaceType.CLASSIC)

        assertEquals(
            listOf(
                ControlPointDefinition(31, ControlPointType.CONTROL, 1),
                ControlPointDefinition(32, ControlPointType.CONTROL, 2),
                ControlPointDefinition(36, ControlPointType.BEACON, 3)
            ),
            controlPoints
        )
        assertEquals("31 32 36B", ControlPointRules.formatControlPoints(controlPoints))
    }

    @Test
    fun rejectsUnknownSpecifiersAndOutOfRangeCodes() {
        assertEquals(
            ControlPointValidationError.UNKNOWN_SPECIFIER,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("31X", RaceType.CLASSIC)
            }.error
        )
        assertEquals(
            ControlPointValidationError.INVALID_RANGE,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("256", RaceType.CLASSIC)
            }.error
        )
    }

    @Test
    fun validatesClassicSequences() {
        assertEquals(
            ControlPointValidationError.CLASSIC_DUPLICATE,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("31 32 31", RaceType.CLASSIC)
            }.error
        )
        assertEquals(
            ControlPointValidationError.NON_LAST_BEACON,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("31 36B 32", RaceType.CLASSIC)
            }.error
        )
        assertEquals(
            ControlPointValidationError.CLASSIC_SPECTATOR_NOT_ALLOWED,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("31 36!", RaceType.CLASSIC)
            }.error
        )
    }

    @Test
    fun validatesSprintSequences() {
        assertEquals(
            ControlPointValidationError.SPRINT_DUPLICATE,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("31 32 31", RaceType.SPRINT)
            }.error
        )

        val multiLap = ControlPointRules.parseControlPoints("31 32 40! 31 32 36B", RaceType.SPRINT)
        assertEquals("31 32 40! 31 32 36B", ControlPointRules.formatControlPoints(multiLap))
    }

    @Test
    fun validatesOrienteeringSequences() {
        assertEquals(
            ControlPointValidationError.ORIENTEERING_SPECIAL,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("31 36B", RaceType.ORIENTEERING)
            }.error
        )
        assertEquals(
            ControlPointValidationError.TWO_IN_ROW,
            assertFailsWith<ControlPointValidationException> {
                ControlPointRules.parseControlPoints("31 31", RaceType.ORIENTEERING)
            }.error
        )
    }

    @Test
    fun formatsDisplayTokensWithOptionalAliases() {
        val tokens = listOf(
            ControlPointDisplayToken(siCode = 31, aliasName = "F1"),
            ControlPointDisplayToken(siCode = 32, aliasName = null),
            ControlPointDisplayToken(siCode = 33, aliasName = "F3", include = false)
        )

        assertEquals("F1 32 ", ControlPointRules.formatDisplayTokens(tokens, useAlias = true))
        assertEquals("31 32 ", ControlPointRules.formatDisplayTokens(tokens, useAlias = false))
    }
}
