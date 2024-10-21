package kolskypavel.ardfmanager.backend.helpers

import android.content.Context
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_MAX_CODE
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_MIN_CODE
import java.util.UUID

object ControlPointsHelper {
    /**
     * Creates a control point from given string
     * Throws Illegal Argument Exception when invalid
     */
    private fun parseControlPoint(
        order: Int,
        controlPointString: String,
        raceId: UUID,
        categoryId: UUID,
        context: Context
    ): ControlPoint {
        val controlPointType =
            when (val lastCharacter = controlPointString.last()) {
                SPECTATOR_CONTROL_MARKER -> ControlPointType.SEPARATOR
                BEACON_CONTROL_MARKER -> ControlPointType.BEACON
                else -> if (lastCharacter.isDigit()) ControlPointType.CONTROL
                else throw IllegalArgumentException(
                    context.getString(R.string.control_point_unknown_specifier, lastCharacter)
                )
            }
        val siCode =
            if (controlPointType == ControlPointType.CONTROL)
                controlPointString.toIntOrNull()
            else
                controlPointString.dropLast(1).toIntOrNull()

        if (siCode == null) {
            throw IllegalArgumentException(
                context.getString(
                    R.string.control_point_unknown_specifier,
                    controlPointString
                )
            )
        }

        if (!(SI_MIN_CODE..SI_MAX_CODE).contains(siCode))
            throw IllegalArgumentException(
                context.getString(R.string.control_point_invalid_range, controlPointString)
            )

        return ControlPoint(
            UUID.randomUUID(),
            raceId,
            categoryId,
            siCode,
            controlPointType,
            order
        )
    }

    /**
     * Validates sequence of controls for orienteering
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateOrienteeringControlSequence(
        controlPoints: List<ControlPoint>,
        context: Context
    ) {
        for (i in 1..<controlPoints.size) {
            val controlPoint = controlPoints[i]
            val previousControlPoint = controlPoints[i - 1]

            if (controlPoint.type != ControlPointType.CONTROL) {
                throw IllegalArgumentException(context.getString(R.string.control_point_orienteering_special))
            }

            if (controlPoint.siCode == previousControlPoint.siCode) {
                throw IllegalArgumentException(context.getString(R.string.control_point_two_in_row))
            }
        }
    }

    /**
     * Validates sequence of controls for classics or foxoring
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateClassicsControlSequence(
        controlPoints: List<ControlPoint>,
        context: Context
    ) {
        if (controlPoints.isEmpty()) {
            return
        }
        val previousCodes = HashSet<Int>()

        for (i in controlPoints.indices) {
            val controlPoint = controlPoints[i]

            if (controlPoint.type == ControlPointType.SEPARATOR) {
                throw IllegalArgumentException(context.getString(R.string.control_point_classics_spectator_not_allowed))
            }

            if (previousCodes.contains(controlPoint.siCode)) {
                throw IllegalArgumentException(context.getString(R.string.control_point_classics_duplicate))
            }

            if (controlPoint.type == ControlPointType.BEACON && i != controlPoints.size - 1) {
                throw IllegalArgumentException(context.getString(R.string.control_point_non_last_beacon))
            }
            previousCodes.add(controlPoint.siCode)
        }
    }

    /**
     * Validates sequence of controls for sprint
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateSprintControlSequence(controlPoints: List<ControlPoint>, context: Context) {
        if (controlPoints.isEmpty()) {
            return
        }

        val previousCodesInLap = HashSet<Int>()
        val previousCodesGlobal = HashSet<Int>()
        previousCodesInLap.add(controlPoints.first().siCode)
        previousCodesGlobal.add(controlPoints.first().siCode)

        for (i in 1..<controlPoints.size) {
            val controlPoint = controlPoints[i]
            val previousControlPoint = controlPoints[i - 1]
            val siCode = controlPoint.siCode

            if (siCode == previousControlPoint.siCode) {
                throw IllegalArgumentException(context.getString(R.string.control_point_two_in_row))
            }

            if (previousCodesInLap.contains(siCode)) {
                throw IllegalArgumentException(context.getString(R.string.control_point_sprint_duplicate))
            }

            when (controlPoint.type) {
                ControlPointType.CONTROL -> {
                    previousCodesInLap.add(siCode)
                    previousCodesGlobal.add(siCode)
                }

                ControlPointType.SEPARATOR -> {
                    if (previousCodesGlobal.contains(siCode)) {
                        throw IllegalArgumentException(
                            context.getString(
                                R.string.control_point_sprint_two_usages,
                                siCode
                            )
                        )
                    }
                    previousCodesInLap.clear()
                }

                ControlPointType.BEACON -> {
                    if (previousCodesGlobal.contains(siCode)) {
                        throw IllegalArgumentException(
                            context.getString(
                                R.string.control_point_non_last_beacon
                            )
                        )
                    }
                    if (i != controlPoints.size - 1) {
                        throw IllegalArgumentException(
                            context.getString(
                                R.string.control_point_non_last_beacon
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Validates sequence of controls
     * If sequence is valid nothing happens, else throws Illegal Argument Exception
     */
    private fun validateControlSequence(
        controlPoints: List<ControlPoint>,
        raceType: RaceType,
        context: Context
    ) {
        when (raceType) {
            RaceType.ORIENTEERING -> validateOrienteeringControlSequence(controlPoints, context)
            RaceType.CLASSICS, RaceType.FOXORING -> validateClassicsControlSequence(
                controlPoints,
                context
            )

            RaceType.SPRINT -> validateSprintControlSequence(controlPoints, context)
        }
    }

    /**
     * Parses an input string into list of controls
     * If sequence is invalid throws Illegal Argument Exception with explanation in the message
     */
    fun getControlPointsFromString(
        input: String,
        raceId: UUID,
        categoryId: UUID,
        raceType: RaceType,
        context: Context
    ): List<ControlPoint> {
        if (input.isEmpty())
            return ArrayList()

        val controlPoints = input.split("\\s+".toRegex()).mapIndexed { index, controlPointString ->
            parseControlPoint(index + 1, controlPointString, raceId, categoryId, context)
        }.toList()

        validateControlSequence(controlPoints, raceType, context)

        return controlPoints
    }

    fun getStringFromControlPoints(controlPoints: List<ControlPoint>): String {
        var codes = ""

        for (cp in controlPoints) {
            codes += cp.siCode

            if (cp.type == ControlPointType.BEACON) {
                codes += "B"
            }
            if (cp.type == ControlPointType.SEPARATOR) {
                codes += "!"
            }
        }
        return codes
    }

    fun getStringFromPunches(punches: List<Punch>): String {
        var string = ""
        for (punch in punches) {
            if (punch.punchType == SIRecordType.CONTROL) {
                string += "${punch.siCode} "
            }
        }
        return string
    }

    const val SPECTATOR_CONTROL_MARKER = '!'
    const val BEACON_CONTROL_MARKER = 'B'
}