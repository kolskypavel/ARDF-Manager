package kolskypavel.ardfmanager.controlpoints

import kolskypavel.ardfmanager.backend.helpers.ControlPointsParser
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import org.junit.Test
import org.junit.Assert.assertThrows
import org.junit.Assert.assertEquals
import java.util.UUID

/**
 * Checks wherever the control point parsing system works
 */
class ControlPointsParsingUnitTest {

    private val controlPointsParser = ControlPointsParser()
    private val raceId = UUID.randomUUID()
    private val categoryId = UUID.randomUUID()

    @Test
    fun testOrienteeringValidParsing() {
        var cpString = "31 32 33 34 35 36 38 40"
        var result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.ORIENTEERING)

        assertEquals(listOf(31, 32, 33, 34, 35, 36, 38, 40), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 8).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(List(8) { ControlPointType.CONTROL }, result.map { cp -> cp.type }.toList())

        cpString = "102 31 49 55 52 35"
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.ORIENTEERING)

        assertEquals(listOf(102, 31, 49, 55, 52, 35), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 6).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(List(6) { ControlPointType.CONTROL }, result.map { cp -> cp.type }.toList())

        cpString = "" // Empty string is fine
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.ORIENTEERING)
        assertEquals(0, result.size)
    }

    @Test
    fun testOrienteeringInvalidParsing() {
        var cpString = "31 32433 34 35 36 38 40" //Invalid range of SI
        System.err.println(assertThrows(IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.ORIENTEERING
            )
        }.message)

        cpString = "22;33;44" //Invalid characters
        System.err.println(assertThrows(IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.ORIENTEERING
            )
        }.message)

        cpString = "#%99%%" //Invalid characters
        System.err.println(assertThrows(IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.ORIENTEERING
            )
        }.message)

        cpString = "44B 33!" //Classics and sprint not valid in orienteering
        System.err.println(assertThrows(IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.ORIENTEERING
            )
        }.message)

        cpString = "33 33" //Same control point in a row
        System.err.println(assertThrows(IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.ORIENTEERING
            )
        }.message)
    }

    @Test
    fun testClassicsValidParsing() {

        var cpString = "31 32 33 34 35 99B"
        var result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.CLASSICS)
        assertEquals(listOf(31, 32, 33, 34, 35, 99), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 6).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(listOf(ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.BEACON), result.map { cp -> cp.type }.toList())

        cpString = ""   // Empty string is fine
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.CLASSICS)
        assertEquals(0, result.size)
    }

    @Test
    fun testClassicsInvalidParsing() {
        var cpString = "31 32 33 34 31 35 99B"
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.CLASSICS
            )
        }.message)
        cpString = "31 32B 33 34 35 99" //Beacon must be the last CP
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.CLASSICS
            )
        }.message)
        cpString = "31 32 33 34! 35 99" //No spectator controls are allowed on classics
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.CLASSICS
            )
        }.message)
        cpString = "32 35 43 44B 99B" //Two beacons
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.CLASSICS
            )
        }.message)
        cpString = "32 35 35 44" //Duplicate control points
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.CLASSICS
            )
        }.message)
        cpString = "32 35 35 44 44B" //Same control point and beacon
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.CLASSICS
            )
        }.message)
        cpString = "#%99%%" //Invalid characters
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.CLASSICS
            )
        }.message)
    }


    @Test
    fun testSprintValidParsing() {
        var cpString = "31 32 33 34 36! 31 35 99B"
        var result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.SPRINT)
        assertEquals(listOf(31, 32, 33, 34, 36, 31, 35, 99), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 8).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(listOf(ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.SEPARATOR, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.BEACON), result.map { cp -> cp.type }.toList())

        cpString = "31 32 33 34 36! 31 35 99"   //Beacon doesn't need to be present
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.SPRINT)
        assertEquals(listOf(31, 32, 33, 34, 36, 31, 35, 99), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 8).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(listOf(ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.SEPARATOR, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL), result.map { cp -> cp.type }.toList())

        cpString = "33 34 35 36"    //No separator is needed
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.SPRINT)
        assertEquals(listOf(33, 34, 35, 36), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 4).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(listOf(ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL), result.map { cp -> cp.type }.toList())

        cpString = "31 32 33 34 36! 31 32 99B"  //Duplicate controls separated
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.SPRINT)
        assertEquals(listOf(31, 32, 33, 34, 36, 31, 32, 99), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 8).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(listOf(ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.SEPARATOR, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.BEACON), result.map { cp -> cp.type }.toList())

        cpString = "31 36! 42 36!"  //Same separators are fine
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.SPRINT)
        assertEquals(listOf(31, 36, 42, 36), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 4).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(listOf(ControlPointType.CONTROL, ControlPointType.SEPARATOR, ControlPointType.CONTROL, ControlPointType.SEPARATOR), result.map { cp -> cp.type }.toList())

        cpString = "31 32 36! 41 42 43 99B"  //Same separator and beacon
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.SPRINT)
        assertEquals(listOf(31, 32, 36, 41, 42, 43, 99), result.map { cp -> cp.siCode }.toList())
        assertEquals((1 .. 7).toList(), result.map { cp -> cp.order }.toList())
        assertEquals(listOf(ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.SEPARATOR, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.CONTROL, ControlPointType.BEACON), result.map { cp -> cp.type }.toList())

        cpString = ""   // Empty string is fine
        result = controlPointsParser.getControlPointsFromString(cpString, raceId, categoryId, RaceType.SPRINT)
        assertEquals(0, result.size)
    }

    @Test
    fun testSprintInvalidParsing() {
        var cpString = "45B 45B"    //Two beacons
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.SPRINT
            )
        }.message)

        cpString = "31 32 33 34 31 36! 31 32 99B"  //Duplicate controls in the same loop
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.SPRINT
            )
        }.message)

        cpString = "#%99%%" //Invalid characters
        System.err.println(assertThrows(java.lang.IllegalArgumentException::class.java) {
            controlPointsParser.getControlPointsFromString(
                cpString,
                raceId,
                categoryId,
                RaceType.SPRINT
            )
        }.message)
    }
}