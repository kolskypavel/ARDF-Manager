package kolskypavel.ardfmanager.backend.files

import android.content.Context
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ReadoutData
import org.openardf.radioomanager.shared.importing.ImportValidationRules
import org.openardf.radioomanager.shared.importing.ReadoutPunchValidationError
import java.util.UUID

/** Validates imported race data before it is written into the Android Room database. */
object DataImportValidator {

    /** Validates one import wrapper for the requested CSV-style data type. */
    @Throws(IllegalArgumentException::class)
    fun validateDataImport(
        data: DataImportWrapper,
        raceId: UUID,
        dataType: DataType,
        dataProcessor: DataProcessor,
        context: Context
    ) {
        when (dataType) {

            // Category names are required and unique; an import may still contain no categories.
            DataType.CATEGORIES -> {
                validateCategories(data.categories, context)
            }

            // SI numbers and start numbers must be unique within the file and the target race.
            DataType.COMPETITORS -> {

                // TODO: Add optional duplicate-name validation once import settings define that policy.
                val duplicateStartNumbers = ImportValidationRules.duplicateStartNumbers(
                    data.competitorCategories.map { it.competitor.startNumber }
                )
                val duplicateSINumbers = ImportValidationRules.duplicateSINumbers(
                    data.competitorCategories.map { it.competitor.siNumber }
                )
                for (comp in data.competitorCategories) {
                    val siNumber = comp.competitor.siNumber
                    val startNumber = comp.competitor.startNumber

                    // Imported rows should already have this race id, but normalize them defensively.
                    if (comp.competitor.raceId != raceId) {
                        comp.competitor.raceId = raceId
                    }

                    // Reject SI numbers duplicated in this file or already present in the race.
                    if (siNumber != null) {
                        if (duplicateSINumbers.contains(siNumber)) {
                            throw IllegalArgumentException(
                                context.getString(
                                    R.string.data_import_competitor_duplicate_si_file,
                                    siNumber
                                )
                            )
                        }
                        if (dataProcessor.checkIfSINumberExists(siNumber, raceId)) {
                            throw IllegalArgumentException(
                                context.getString(
                                    R.string.data_import_competitor_duplicate_si_race,
                                    siNumber
                                )
                            )
                        }
                    }

                    // Reject start numbers duplicated in this file or already present in the race.
                    if (duplicateStartNumbers.contains(startNumber)) {
                        throw IllegalArgumentException(
                            context.getString(
                                R.string.data_import_competitor_duplicate_start_number_file,
                                startNumber
                            )
                        )
                    }

                    if (dataProcessor.checkIfStartNumberExists(startNumber, raceId)) {
                        throw IllegalArgumentException(
                            context.getString(
                                R.string.data_import_competitor_duplicate_start_number_race,
                                startNumber
                            )
                        )
                    }
                }
            }

            DataType.COMPETITOR_STARTS -> {
                // TODO: Implement once import settings define how strict start-time updates should be.
            }

            else -> {
                throw IllegalArgumentException(context.getString(R.string.data_import_format_not_supported))
            }
        }
    }

    /** Validates a full race import, including race metadata, aliases, competitors, and readouts. */
    @Throws(IllegalArgumentException::class)
    fun validateRaceDataImport(
        raceData: RaceData,
        context: Context
    ) {
        val race = raceData.race

        if (race.name.isEmpty()) {
            throw IllegalArgumentException(context.getString(R.string.data_import_race_blank_name))
        }

        validateCategories(raceData.categories, context)
        validateRaceDataAliases(raceData.aliases, context)
        validateRaceDataCompetitors(raceData.competitorData, race.id, context)
        for (unmatched in raceData.unmatchedReadoutData) {
            validateRaceDataReadoutData(unmatched, race.id, context)
        }
    }

    /** Rejects duplicate category names within one import payload. */
    @Throws(IllegalArgumentException::class)
    fun validateCategories(categories: List<CategoryData>, context: Context) {
        val names = categories.map { it.category.name }

        val catNames = ImportValidationRules.duplicateCategoryNames(names)
        if (catNames.isNotEmpty()) {
            throw IllegalArgumentException(
                context.getString(
                    R.string.data_import_category_duplicate,
                    catNames.joinToString(", ")
                )
            )
        }
    }

    /** Validates imported competitors and any readouts nested under them. */
    @Throws(IllegalArgumentException::class)
    fun validateRaceDataCompetitors(
        competitors: List<CompetitorData>,
        raceId: UUID,
        context: Context
    ) {

        val duplicateStartNumbers = ImportValidationRules.duplicateStartNumbers(
            competitors.map { it.competitorCategory.competitor.startNumber }
        )
        val duplicateSINumbers = ImportValidationRules.duplicateSINumbers(
            competitors.map { it.competitorCategory.competitor.siNumber }
        )

        for (comp in competitors) {
            val siNumber = comp.competitorCategory.competitor.siNumber
            val startNumber = comp.competitorCategory.competitor.startNumber

            // Imported rows should already have this race id, but normalize them defensively.
            if (comp.competitorCategory.competitor.raceId != raceId) {
                comp.competitorCategory.competitor.raceId = raceId
            }

            // Full-race imports are self-contained, so only check duplicates inside the payload.
            if (siNumber != null) {
                if (duplicateSINumbers.contains(siNumber)) {
                    throw IllegalArgumentException(
                        context.getString(
                            R.string.data_import_competitor_duplicate_si_file,
                            siNumber
                        )
                    )
                }
            }

            // Start numbers still need to be unique inside the payload.
            if (duplicateStartNumbers.contains(startNumber)) {
                throw IllegalArgumentException(
                    context.getString(
                        R.string.data_import_competitor_duplicate_start_number_file,
                        startNumber
                    )
                )
            }

            // Readouts nested under competitors must obey the same punch-shape rules as unmatched readouts.
            comp.readoutData?.let { validateRaceDataReadoutData(it, raceId, context) }
        }
    }

    /** Validates one imported readout and normalizes child race/result identifiers. */
    @Throws(IllegalArgumentException::class)
    fun validateRaceDataReadoutData(
        readoutData: ReadoutData,
        raceId: UUID,
        context: Context
    ) {

        val result = readoutData.result
        val punches = readoutData.punches.map { it -> it.punch }

        // Normalize ids so child rows can be inserted under the newly imported race.
        if (result.raceId != raceId) {
            result.raceId = raceId
        }

        for (punch in punches) {
            if (punch.raceId != raceId) {
                punch.raceId = raceId
            }
            if (punch.resultId != result.id) {
                punch.resultId = result.id
            }
        }

        val punchErrors = ImportValidationRules.validateReadoutPunchTypes(punches.map { it.punchType })
        if (punchErrors.contains(ReadoutPunchValidationError.MULTIPLE_START)) {
            throw IllegalArgumentException(
                context.getString(
                    R.string.data_import_readout_multiple_start,
                    result.siNumber ?: "?"
                )
            )
        }
        if (punchErrors.contains(ReadoutPunchValidationError.MULTIPLE_FINISH)) {
            throw IllegalArgumentException(
                context.getString(
                    R.string.data_import_readout_multiple_finish,
                    result.siNumber ?: "?"
                )
            )
        }
    }

    /** Rejects aliases with duplicate names or SI codes inside a full-race import. */
    @Throws(IllegalArgumentException::class)
    fun validateRaceDataAliases(aliases: List<Alias>, context: Context) {

        val names = aliases.map { it.name }
        val codes = aliases.map { it.siCode }

        // Collect both duplicate dimensions so the user sees all alias conflicts at once.
        val duplicateNames = ImportValidationRules.duplicateAliasNames(names)
        val duplicateCodes = ImportValidationRules.duplicateAliasCodes(codes)

        val errors = mutableListOf<String>()
        if (duplicateNames.isNotEmpty()) {
            errors.add(
                context.getString(
                    R.string.data_import_alias_name_duplicate,
                    duplicateNames.joinToString(", ")
                )
            )
        }
        if (duplicateCodes.isNotEmpty()) {
            errors.add(
                context.getString(
                    R.string.data_import_alias_code_duplicate,
                    duplicateCodes.joinToString(", ")
                )
            )
        }

        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString("\n"))
        }
    }
}
