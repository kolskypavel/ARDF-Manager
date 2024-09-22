package kolskypavel.ardfmanager.controlpoints

import org.junit.Test

/**
 * Checks wherever the control point parsing system works
 */

//TODO:adjust to the actual implemetation
class ControlPointsParsingUnitTest {

    @Test
    fun testOrienteeringValidParsing() {
        var cpString = "31 32 33 34 35 36 38 40"

        cpString = "102 25 49 55 52 35"

        cpString = "" // Empty string is fine

    }

    @Test
    fun testOrienteeringInvalidParsing() {
        var cpString = "31 32433 34 35 36 38 40" //Invalid range of SI

        cpString = "22;33;44" //Invalid characters

        cpString = "#%99%%" //Invalid characters

        cpString = "44B 33!" //Classics and sprint not valid in orienteering

        cpString = "33 33" //Same control point in a row

    }

    @Test
    fun testClassicsValidParsing() {

        var cpString = "31 32 33 34 35 99B"
        cpString = ""   // Empty string is fine
    }

    @Test
    fun testClassicsInvalidParsing() {
        var cpString = "31 32 33 34 31 35 99B"
        cpString = "31 32B 33 34 35 99" //Beacon must be the last CP
        cpString = "31 32 33 34! 35 99" //No spectator controls are allowed on classics
        cpString = "32 35 43 44B 99B" //Two beacons
        cpString = "32 35 35 44" //Duplicate control points
        cpString = "32 35 35 44 44B" //Same control point and beacon
        cpString = "#%99%%" //Invalid characters

    }


    @Test
    fun testSprintValidParsing() {
        var cpString = "31 32 33 34 36! 31 35 99B"
        cpString = "31 32 33 34 36! 31 35 99"   //Beacon doesn't need to be present

        cpString = "33 34 35 36"    //No separator is needed
        cpString = "31 32 33 34 36! 31 32 99B"  //Duplicate controls separated
        cpString = "31 36! 42 36!"  //Same separators are fine
        cpString = "31 32 36! 41 42 43 99B"  //Same separator and beacon
        cpString = ""   // Empty string is fine
    }

    @Test
    fun testSprintInvalidParsing() {
        var cpString = "45B 45B"    //Two beacons
        cpString = "31 32 33 34 31 36! 31 32 99B"  //Duplicate controls in the same loop
        cpString = "#%99%%" //Invalid characters

    }
}