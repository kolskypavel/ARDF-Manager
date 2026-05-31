package org.openardf.radioomanager.shared.course

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.sportident.SportIdentCodes

object ControlPointRules {
    const val SPECTATOR_CONTROL_MARKER = '!'
    const val BEACON_CONTROL_MARKER = 'B'

    fun parseControlPoints(input: String, raceType: RaceType): List<ControlPointDefinition> {
        if (input.isEmpty()) {
            return emptyList()
        }

        val controlPoints = input.split("\\s+".toRegex()).mapIndexed { index, token ->
            parseControlPoint(index + 1, token)
        }

        validateControlSequence(controlPoints, raceType)
        return controlPoints
    }

    fun formatControlPoints(controlPoints: List<ControlPointDefinition>): String {
        return controlPoints.joinToString(" ") { controlPoint ->
            val marker = when (controlPoint.type) {
                ControlPointType.BEACON -> BEACON_CONTROL_MARKER.toString()
                ControlPointType.SEPARATOR -> SPECTATOR_CONTROL_MARKER.toString()
                ControlPointType.CONTROL -> ""
            }
            "${controlPoint.siCode}$marker"
        }
    }

    fun formatDisplayTokens(tokens: List<ControlPointDisplayToken>, useAlias: Boolean): String {
        val builder = StringBuilder()

        for ((index, token) in tokens.withIndex()) {
            if (token.include) {
                builder.append(
                    if (useAlias && token.aliasName != null) {
                        token.aliasName
                    } else {
                        token.siCode.toString()
                    }
                )
            }

            if (index < tokens.size - 1) {
                builder.append(" ")
            }
        }

        return builder.toString()
    }

    private fun parseControlPoint(order: Int, token: String): ControlPointDefinition {
        val controlPointType =
            when (val lastCharacter = token.last()) {
                SPECTATOR_CONTROL_MARKER -> ControlPointType.SEPARATOR
                BEACON_CONTROL_MARKER -> ControlPointType.BEACON
                else -> if (lastCharacter.isDigit()) ControlPointType.CONTROL
                else throw ControlPointValidationException(
                    ControlPointValidationError.UNKNOWN_SPECIFIER,
                    token = lastCharacter.toString()
                )
            }

        val siCode =
            if (controlPointType == ControlPointType.CONTROL) {
                token.toIntOrNull()
            } else {
                token.dropLast(1).toIntOrNull()
            } ?: throw ControlPointValidationException(
                ControlPointValidationError.UNKNOWN_SPECIFIER,
                token = token
            )

        if (!SportIdentCodes.isSICodeValid(siCode)) {
            throw ControlPointValidationException(
                ControlPointValidationError.INVALID_RANGE,
                token = token
            )
        }

        return ControlPointDefinition(siCode, controlPointType, order)
    }

    private fun validateControlSequence(controlPoints: List<ControlPointDefinition>, raceType: RaceType) {
        when (raceType) {
            RaceType.ORIENTEERING -> validateOrienteeringControlSequence(controlPoints)
            RaceType.CLASSIC, RaceType.SHORT, RaceType.FOXORING -> validateClassicsControlSequence(controlPoints)
            RaceType.SPRINT -> validateSprintControlSequence(controlPoints)
        }
    }

    private fun validateOrienteeringControlSequence(controlPoints: List<ControlPointDefinition>) {
        for (i in 1..<controlPoints.size) {
            val controlPoint = controlPoints[i]
            val previousControlPoint = controlPoints[i - 1]

            if (controlPoint.type != ControlPointType.CONTROL) {
                throw ControlPointValidationException(ControlPointValidationError.ORIENTEERING_SPECIAL)
            }

            if (controlPoint.siCode == previousControlPoint.siCode) {
                throw ControlPointValidationException(ControlPointValidationError.TWO_IN_ROW)
            }
        }
    }

    private fun validateClassicsControlSequence(controlPoints: List<ControlPointDefinition>) {
        if (controlPoints.isEmpty()) {
            return
        }

        val previousCodes = HashSet<Int>()
        for (i in controlPoints.indices) {
            val controlPoint = controlPoints[i]

            if (controlPoint.type == ControlPointType.SEPARATOR) {
                throw ControlPointValidationException(ControlPointValidationError.CLASSIC_SPECTATOR_NOT_ALLOWED)
            }

            if (previousCodes.contains(controlPoint.siCode)) {
                throw ControlPointValidationException(ControlPointValidationError.CLASSIC_DUPLICATE)
            }

            if (controlPoint.type == ControlPointType.BEACON && i != controlPoints.size - 1) {
                throw ControlPointValidationException(ControlPointValidationError.NON_LAST_BEACON)
            }
            previousCodes.add(controlPoint.siCode)
        }
    }

    private fun validateSprintControlSequence(controlPoints: List<ControlPointDefinition>) {
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
                throw ControlPointValidationException(ControlPointValidationError.TWO_IN_ROW)
            }

            if (previousCodesInLap.contains(siCode)) {
                throw ControlPointValidationException(ControlPointValidationError.SPRINT_DUPLICATE)
            }

            when (controlPoint.type) {
                ControlPointType.CONTROL -> {
                    previousCodesInLap.add(siCode)
                    previousCodesGlobal.add(siCode)
                }

                ControlPointType.SEPARATOR -> {
                    if (previousCodesGlobal.contains(siCode)) {
                        throw ControlPointValidationException(
                            ControlPointValidationError.SPRINT_SPECIAL_REUSES_CONTROL,
                            siCode = siCode
                        )
                    }
                    previousCodesInLap.clear()
                }

                ControlPointType.BEACON -> {
                    if (previousCodesGlobal.contains(siCode) || i != controlPoints.size - 1) {
                        throw ControlPointValidationException(ControlPointValidationError.NON_LAST_BEACON)
                    }
                }
            }
        }
    }
}

data class ControlPointDisplayToken(
    val siCode: Int,
    val aliasName: String? = null,
    val include: Boolean = true
)

class ControlPointValidationException(
    val error: ControlPointValidationError,
    val token: String? = null,
    val siCode: Int? = null
) : IllegalArgumentException()

enum class ControlPointValidationError {
    UNKNOWN_SPECIFIER,
    INVALID_RANGE,
    TWO_IN_ROW,
    ORIENTEERING_SPECIAL,
    CLASSIC_DUPLICATE,
    NON_LAST_BEACON,
    CLASSIC_SPECTATOR_NOT_ALLOWED,
    SPRINT_DUPLICATE,
    SPRINT_SPECIAL_REUSES_CONTROL
}
