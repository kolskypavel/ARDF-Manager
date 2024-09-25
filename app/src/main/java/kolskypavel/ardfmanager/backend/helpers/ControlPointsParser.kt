package kolskypavel.ardfmanager.backend.helpers

import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_MAX_CODE
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_MIN_CODE
import java.util.UUID

class ControlPointsParser {
    /**
     * Creates a control point from given string
     * Throws Illegal Argument Exception when invalid
     */
    private fun parseControlPoint(order : Int, controlPointString : String, raceId : UUID, categoryId : UUID) : ControlPoint
    {
        val controlPointType =
            when (val lastCharacter = controlPointString.last())
            {
                SPECTATOR_CONTROL_MARKER -> ControlPointType.SEPARATOR
                BEACON_CONTROL_MARKER -> ControlPointType.BEACON
                else -> if (lastCharacter.isDigit()) ControlPointType.CONTROL
                else throw IllegalArgumentException(
                    "Unknown special control specifier $lastCharacter"
                )
            }
        val siCode =
            if (controlPointType == ControlPointType.CONTROL)
                controlPointString.toIntOrNull()
            else
                controlPointString.dropLast(1).toIntOrNull()

        if (siCode == null) {
            throw IllegalArgumentException("Can not parse SI Code from $controlPointString")
        }

        if (!(SI_MIN_CODE..SI_MAX_CODE).contains(siCode))
            throw IllegalArgumentException("SI Code $controlPointString out of range")

        //TODO: Get name

        return ControlPoint(UUID.randomUUID(), raceId, categoryId, siCode, null, controlPointType, order)
    }

    /**
     * Validates sequence of controls for orienteering
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateOrienteeringControlSequence(controlPoints : List<ControlPoint>) : Unit
    {
        for (i in 1..< controlPoints.size)
        {
            val controlPoint = controlPoints[i]
            val previousControlPoint = controlPoints[i - 1]

            if (controlPoint.type != ControlPointType.CONTROL) {
                throw IllegalArgumentException("Orienteering races cannot include special control points")
            }

            if (controlPoint.siCode == previousControlPoint.siCode) {
                throw IllegalArgumentException("Two identical controls in a row")
            }
        }
    }

    /**
     * Validates sequence of controls for classics or foxoring
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateClassicsControlSequence(controlPoints : List<ControlPoint>) : Unit {
        if (controlPoints.isEmpty()) {
            return
        }

        val previousCodes = HashSet<Int>()
        previousCodes.add(controlPoints.first().siCode)

        for (i in 1..< controlPoints.size)
        {
            val controlPoint = controlPoints[i]
            val previousControlPoint = controlPoints[i - 1]

            if (controlPoint.siCode == previousControlPoint.siCode) {
                throw IllegalArgumentException("Two identical controls in a row")
            }

            if (previousCodes.contains(controlPoint.siCode)) {
                throw IllegalArgumentException("Duplicate controls are not allowed in a classical race")
            }

            if (controlPoint.type != ControlPointType.CONTROL) {
                if (i != controlPoints.size - 1)
                    throw IllegalArgumentException("Non-last control point on a classical race cannot be a special control")
                if (controlPoint.type != ControlPointType.BEACON)
                    throw IllegalArgumentException("Spectator control points are not allowed on a classical race")
            }

            previousCodes.add(controlPoint.siCode)
        }
    }

    /**
     * Validates sequence of controls for sprint
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateSprintControlSequence(controlPoints : List<ControlPoint>) : Unit
    {
        if (controlPoints.isEmpty()) {
            return
        }

        val previousCodesInLap = HashSet<Int>()
        val previousCodesGlobal = HashSet<Int>()
        previousCodesInLap.add(controlPoints.first().siCode)
        previousCodesGlobal.add(controlPoints.first().siCode)

        for (i in 1..< controlPoints.size)
        {
            val controlPoint = controlPoints[i]
            val previousControlPoint = controlPoints[i - 1]
            val siCode = controlPoint.siCode

            if (siCode == previousControlPoint.siCode) {
                throw IllegalArgumentException("Two identical controls in a row")
            }

            if (previousCodesInLap.contains(siCode)) {
                throw IllegalArgumentException("Duplicate controls are not allowed in a single lap of a sprint race")
            }

            when (controlPoint.type) {
                ControlPointType.CONTROL -> {
                    previousCodesInLap.add(siCode)
                    previousCodesGlobal.add(siCode)
                }
                ControlPointType.SEPARATOR -> {
                    if (previousCodesGlobal.contains(siCode)) {
                        throw IllegalArgumentException("Control point with SI code $siCode used as a separator and as a control")
                    }
                    previousCodesInLap.clear()
                }
                ControlPointType.BEACON -> {
                    if (previousCodesGlobal.contains(siCode)) {
                        throw IllegalArgumentException("Control point with SI code $siCode used as a beacon and as a control")
                    }
                    if (i != controlPoints.size - 1) {
                        throw IllegalArgumentException("Beacon must be last")
                    }
                }
            }
        }
    }

    /**
     * Validates sequence of controls
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateControlSequence(controlPoints : List<ControlPoint>, raceType : RaceType)
    {
        when (raceType)
        {
            RaceType.ORIENTEERING -> validateOrienteeringControlSequence(controlPoints)
            RaceType.CLASSICS, RaceType.FOXORING -> validateClassicsControlSequence(controlPoints)
            RaceType.SPRINT -> validateSprintControlSequence(controlPoints)
        }
    }

    /**
     * Parses an input string into list of controls
     * If sequence is invalid throws Illegal Argument Exception with explanation in the message
     */
    fun getControlPointsFromString(input : String, raceId : UUID, categoryId : UUID, raceType : RaceType) : List<ControlPoint>
    {
        if (input.isEmpty())
            return ArrayList()

        val controlPoints = input.split("\\s+".toRegex()).mapIndexed{
            index, controlPointString -> parseControlPoint(index + 1, controlPointString, raceId, categoryId)
        }.toList()

        validateControlSequence(controlPoints, raceType)

        return controlPoints
    }

    companion object {
        private const val SPECTATOR_CONTROL_MARKER = '!'
        private const val BEACON_CONTROL_MARKER = 'B'
    }
}