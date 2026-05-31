package kolskypavel.ardfmanager.backend.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ControlPointAlias
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import org.openardf.radioomanager.shared.course.ControlPointDefinition
import org.openardf.radioomanager.shared.course.ControlPointDisplayToken
import org.openardf.radioomanager.shared.course.ControlPointRules
import org.openardf.radioomanager.shared.course.ControlPointValidationError
import org.openardf.radioomanager.shared.course.ControlPointValidationException
import java.util.UUID

/**
 * @author Vojtech Kopal, Pavel Kolsky
 * General helper for control points - parsing, validation, export to string, etc.
 */
object ControlPointsHelper {
    /**
     * Parses an input string into list of controls
     * If sequence is invalid throws Illegal Argument Exception with explanation in the message
     */
    fun getControlPointsFromString(
        input: String,
        categoryId: UUID,
        raceType: RaceType,
        context: Context
    ): List<ControlPoint> {
        return try {
            ControlPointRules.parseControlPoints(input, raceType).map { definition ->
                definition.toControlPoint(categoryId)
            }
        } catch (exception: ControlPointValidationException) {
            throw IllegalArgumentException(exception.toLocalizedMessage(context))
        }
    }

    fun getStringFromControlPoints(controlPoints: List<ControlPoint>): String {
        return ControlPointRules.formatControlPoints(
            controlPoints.map { controlPoint ->
                ControlPointDefinition(controlPoint.siCode, controlPoint.type, controlPoint.order)
            }
        )
    }

    private fun ControlPointDefinition.toControlPoint(categoryId: UUID): ControlPoint {
        return ControlPoint(
            UUID.randomUUID(),
            categoryId,
            siCode,
            type,
            order
        )
    }

    private fun ControlPointValidationException.toLocalizedMessage(context: Context): String {
        return when (error) {
            ControlPointValidationError.UNKNOWN_SPECIFIER ->
                context.getString(R.string.control_point_unknown_specifier, token)

            ControlPointValidationError.INVALID_RANGE ->
                context.getString(R.string.control_point_invalid_range, token)

            ControlPointValidationError.TWO_IN_ROW ->
                context.getString(R.string.control_point_two_in_row)

            ControlPointValidationError.ORIENTEERING_SPECIAL ->
                context.getString(R.string.control_point_orienteering_special)

            ControlPointValidationError.CLASSIC_DUPLICATE ->
                context.getString(R.string.control_point_classic_duplicate)

            ControlPointValidationError.NON_LAST_BEACON ->
                context.getString(R.string.control_point_non_last_beacon)

            ControlPointValidationError.CLASSIC_SPECTATOR_NOT_ALLOWED ->
                context.getString(R.string.control_point_classic_spectator_not_allowed)

            ControlPointValidationError.SPRINT_DUPLICATE ->
                context.getString(R.string.control_point_sprint_duplicate)

            ControlPointValidationError.SPRINT_SPECIAL_REUSES_CONTROL ->
                context.getString(R.string.control_point_sprint_two_usages, siCode)
        }
    }

    fun getStringFromControlPointAliases(
        controlPoints: List<ControlPointAlias>,
        context: Context
    ): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val useAlias =
            sharedPref.getBoolean(context.getString(R.string.key_results_use_aliases), true)

        return ControlPointRules.formatDisplayTokens(
            controlPoints.map { controlPointAlias ->
                ControlPointDisplayToken(
                    siCode = controlPointAlias.controlPoint.siCode,
                    aliasName = controlPointAlias.alias?.name
                )
            },
            useAlias
        )
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

    fun getStringFromAliasPunches(punches: List<AliasPunch>, context: Context): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val useAlias =
            sharedPref.getBoolean(context.getString(R.string.key_results_use_aliases), true)

        return ControlPointRules.formatDisplayTokens(
            punches.map { aliasPunch ->
                ControlPointDisplayToken(
                    siCode = aliasPunch.punch.siCode,
                    aliasName = aliasPunch.alias?.name,
                    include = aliasPunch.punch.punchType == SIRecordType.CONTROL
                )
            },
            useAlias
        )
    }

    const val SPECTATOR_CONTROL_MARKER = ControlPointRules.SPECTATOR_CONTROL_MARKER
    const val BEACON_CONTROL_MARKER = ControlPointRules.BEACON_CONTROL_MARKER
}
