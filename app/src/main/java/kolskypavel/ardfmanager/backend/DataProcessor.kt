package kolskypavel.ardfmanager.backend

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.files.FileProcessor
import kolskypavel.ardfmanager.backend.files.constants.DataFormat
import kolskypavel.ardfmanager.backend.files.constants.DataType
import kolskypavel.ardfmanager.backend.files.wrappers.DataImportWrapper
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.prints.PrintProcessor
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.ARDFRepository
import kolskypavel.ardfmanager.backend.room.entitity.Alias
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.RaceBand
import kolskypavel.ardfmanager.backend.room.enums.RaceLevel
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.FinishTimeSource
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.StartTimeSource
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SIReaderService
import kolskypavel.ardfmanager.backend.sportident.SIReaderState
import kolskypavel.ardfmanager.backend.sportident.SIReaderStatus
import kolskypavel.ardfmanager.backend.wrappers.StatisticsWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID


/**
 * This is the main backend interface, processing and providing various sources of data
 */
class DataProcessor private constructor(context: Context) {

    private val ardfRepository = ARDFRepository.get()
    private var appContext: WeakReference<Context> = WeakReference(context)

    var currentState = MutableLiveData<AppState>()
    var resultsProcessor: ResultsProcessor? = null
    var fileProcessor: FileProcessor? = null
    var printProcessor = PrintProcessor()

    companion object {
        private var INSTANCE: DataProcessor? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = DataProcessor(context)
            }
        }

        fun get(): DataProcessor {
            return INSTANCE ?: throw IllegalStateException("DataProcessor must be initialized")
        }
    }

    init {
        currentState.postValue(
            AppState(null, SIReaderState(SIReaderStatus.DISCONNECTED))
        )
    }

    fun updateReaderState(newSIState: SIReaderState) {
        val stateToUpdate = currentState.value

        if (stateToUpdate != null) {
            stateToUpdate.siReaderState = newSIState
            currentState.postValue(stateToUpdate)
        }
    }

    suspend fun setCurrentRace(raceId: UUID): Race {
        val race = getRace(raceId)
        currentState.postValue(currentState.value?.let { AppState(race, it.siReaderState) })

        return race
    }

    fun getCurrentRace() = currentState.value?.currentRace!!

    fun removeReaderRace() {
        currentState.postValue(currentState.value?.let { AppState(null, it.siReaderState) })
    }

    //METHODS TO HANDLE RACES
    fun getRaces(): Flow<List<Race>> = ardfRepository.getRaces()

    private suspend fun getRace(id: UUID): Race = ardfRepository.getRace(id)

    suspend fun createRace(race: Race) = ardfRepository.createRace(race)


    suspend fun updateRace(race: Race) {
        ardfRepository.updateRace(race)
        updateResults(race.id)
    }

    suspend fun deleteRace(id: UUID) {
        ardfRepository.deleteRace(id)
    }

    //CATEGORIES
    fun getCategoryDataFlowForRace(raceId: UUID) =
        ardfRepository.getCategoryDataFlowForRace(raceId)

    suspend fun getCategory(id: UUID): Category? = ardfRepository.getCategory(id)

    suspend fun getCategoriesForRace(raceId: UUID) = ardfRepository.getCategoriesForRace(raceId)

    suspend fun getCategoryData(id: UUID, raceId: UUID): CategoryData? {
        return ardfRepository.getCategoryData(id, raceId)
    }

    suspend fun getCategoryDataForRace(raceId: UUID): List<CategoryData> =
        ardfRepository.getCategoryDataForRace(raceId)


    suspend fun getCategoryByName(string: String, raceId: UUID) =
        ardfRepository.getCategoryByName(string, raceId)

    suspend fun getCategoryByBirthYear(birthYear: Int, isWoman: Boolean, raceId: UUID): Category? {
        //Calculate the age difference
        val age = LocalDate.now().year - birthYear
        return ardfRepository.getCategoryByBirthYear(age, isWoman, raceId)
    }

    suspend fun getStartTimeForCategory(categoryId: UUID): Duration? {
        val competitors = ardfRepository.getCompetitorsByCategory(categoryId)
            .sortedBy { it.drawnRelativeStartTime }

        return if (competitors.isNotEmpty()) {
            competitors.first().drawnRelativeStartTime
        } else null
    }

    suspend fun getHighestCategoryOrder(raceId: UUID) =
        ardfRepository.getHighestCategoryOrder(raceId)

    suspend fun getCategoryByMaxAge(maxAge: Int, raceId: UUID) =
        ardfRepository.getCategoryByMaxAge(maxAge, raceId)

    suspend fun createOrUpdateCategory(category: Category, controlPoints: List<ControlPoint>?) {
        ardfRepository.createOrUpdateCategory(category, controlPoints)
        updateResultsForCategory(category.id, false)
    }

    /**
     * Creates a duplicate of the category
     */
    suspend fun duplicateCategory(categoryData: CategoryData) {
        categoryData.category.name += "_" + (appContext.get()?.getString(R.string.copy) ?: "_copy")
        categoryData.category.order = getHighestCategoryOrder(categoryData.category.raceId) + 1

        //Adjust the IDs
        categoryData.category.id = UUID.randomUUID()
        for (cp in categoryData.controlPoints) {
            cp.id = UUID.randomUUID()
            cp.categoryId = categoryData.category.id
        }

        createOrUpdateCategory(categoryData.category, categoryData.controlPoints)
    }

    suspend fun deleteCategory(id: UUID, raceId: UUID) {
        ardfRepository.deleteCategory(id)
        ardfRepository.deleteControlPointsByCategory(id)
        updateResultsForCategory(id, true)
        updateCategoryOrder(raceId)
    }

    //Updates category order after one is deleted - starts at 0
    private suspend fun updateCategoryOrder(raceId: UUID) {
        val categories = ardfRepository.getCategoriesForRace(raceId)
        for (c in categories.withIndex()) {
            c.value.order = c.index
            ardfRepository.createOrUpdateCategory(c.value,null)
        }
    }

    //CONTROL POINTS
    suspend fun getControlPointsByCategory(categoryId: UUID) =
        ardfRepository.getControlPointsByCategory(categoryId)


    fun adjustControlPoints(
        controlPoints: ArrayList<ControlPoint>,
        raceType: RaceType
    ) = ResultsProcessor.adjustControlPoints(controlPoints, raceType)


    suspend fun getControlPointByCode(raceId: UUID, code: Int) =
        ardfRepository.getControlPointByCode(raceId, code)


    fun getCodesNameFromControlPoints(controlPoints: List<ControlPoint>): String {
        return ResultsProcessor.getCodesNameFromControlPoints(controlPoints)
    }

    //ALIASES
    suspend fun getAliasesByRace(raceId: UUID) = ardfRepository.getAliasesByRace(raceId)

    suspend fun createOrUpdateAliases(aliases: List<Alias>, raceId: UUID) {
        ardfRepository.deleteAliasesByRace(raceId)
        for (alias in aliases) {
            ardfRepository.createOrUpdateAlias(alias)
        }
    }

    //COMPETITORS
    fun getCompetitorDataFlowByRace(raceId: UUID) =
        ardfRepository.getCompetitorDataFlowByRace(raceId)

    suspend fun getCompetitor(id: UUID) = ardfRepository.getCompetitor(id)

    suspend fun getCompetitorBySINumber(siNumber: Int, raceId: UUID): Competitor? =
        ardfRepository.getCompetitorBySINumber(siNumber, raceId)

    suspend fun getCompetitorsByCategory(categoryId: UUID): List<Competitor> =
        ardfRepository.getCompetitorsByCategory(categoryId)

    suspend fun getStatisticsByRace(raceId: UUID): StatisticsWrapper {
        val competitors = ardfRepository.getCompetitorDataByRace(raceId)
        val statistics = StatisticsWrapper(competitors.size, 0, 0, 0)

        for (cd in competitors) {
            val competitor = cd.competitorCategory.competitor
            val category = cd.competitorCategory.category

            if (cd.readoutResult == null) {
                if (competitor.drawnRelativeStartTime != null) {
                    //Count started
                    if (TimeProcessor.hasStarted(
                            getCurrentRace().startDateTime,
                            competitor.drawnRelativeStartTime!!,
                            LocalDateTime.now()
                        )
                    ) {
                        statistics.startedCompetitors++
                    }

                    val limit = category?.timeLimit ?: getCurrentRace().timeLimit
                    if (TimeProcessor.isInLimit(
                            getCurrentRace().startDateTime,
                            competitor.drawnRelativeStartTime!!,
                            limit, LocalDateTime.now()
                        )
                    ) {
                        statistics.inLimitCompetitors++
                    }
                }
            } else {
                statistics.startedCompetitors++
                statistics.finishedCompetitors++
            }

        }
        return statistics
    }

    fun checkIfSINumberExists(siNumber: Int, raceId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfSINumberExists(siNumber, raceId) > 0
        }
    }

    fun checkIfStartNumberExists(startNumber: Int, raceId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfStartNumberExists(startNumber, raceId) > 0
        }
    }

    suspend fun getHighestStartNumberByRace(raceId: UUID) =
        ardfRepository.getHighestStartNumberByRace(raceId)

    suspend fun createOrUpdateCompetitor(
        competitor: Competitor,
    ) {
        ardfRepository.createCompetitor(competitor)
        updateResultsForCompetitor(competitor.id)
    }

    suspend fun deleteCompetitor(id: UUID, deleteReadout: Boolean) {
        ardfRepository.deleteCompetitor(id)
        // TODO: solve the removal of the readout
        if (deleteReadout) {
            ardfRepository.deleteReadoutForCompetitor(id)
        }
    }

    suspend fun deleteAllCompetitorsByRace(raceId: UUID) {
        ardfRepository.deleteAllCompetitorsByRace(raceId)
    }

    //READOUTS
    fun getReadoutDataFlowByRace(raceId: UUID): Flow<List<ReadoutData>> {
        return ardfRepository.getReadoutDataByRace(raceId)
    }

    suspend fun getReadoutDataByReadout(readoutId: UUID): ReadoutData? =
        ardfRepository.getReadoutDataByReadout(readoutId)

    suspend fun getReadoutBySINumber(siNumber: Int, raceId: UUID): Readout? =
        ardfRepository.getReadoutBySINumber(siNumber, raceId)

    suspend fun getReadoutByCompetitor(competitorId: UUID): Readout? =
        ardfRepository.getReadoutsByCompetitor(competitorId)


    suspend fun saveReadoutAndResult(readout: Readout, punches: ArrayList<Punch>, result: Result) =
        ardfRepository.saveReadoutAndResult(readout, punches, result)

    fun checkIfReadoutExistsBySI(siNumber: Int, raceId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfReadoutExistsById(siNumber, raceId) > 0
        }
    }

    suspend fun deleteReadout(id: UUID) = ardfRepository.deleteReadout(id)


    suspend fun deleteAllReadoutsByRace(raceId: UUID) {
        ardfRepository.deleteAllReadoutsByRace(raceId)
    }

    //PUNCHES
    suspend fun getPunchesByReadout(readoutId: UUID) =
        ardfRepository.getPunchesByReadout(readoutId)

    suspend fun getPunchesByCompetitor(competitorId: UUID) =
        ardfRepository.getPunchesByCompetitor(competitorId)

    private suspend fun createPunch(punch: Punch) = ardfRepository.createPunch(punch)

    suspend fun createPunches(punches: ArrayList<Punch>) {
        punches.forEach { punch -> createPunch(punch) }
    }

    suspend fun processCardData(cardData: CardData, race: Race) =
        appContext.get()?.let { resultsProcessor?.processCardData(cardData, race, it) }

    suspend fun processManualPunches(
        readout: Readout,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) = resultsProcessor?.processManualPunchData(readout, punches, manualStatus)

    //RESULTS
    fun getResultDataFlowByRace(raceId: UUID) = resultsProcessor!!.getResultDataByRace(raceId)

    suspend fun getResultByCompetitor(competitorId: UUID) =
        ardfRepository.getResultByCompetitor(competitorId)

    suspend fun getResultByReadout(readoutId: UUID) = ardfRepository.getResultByReadout(readoutId)

    suspend fun createResult(result: Result) = ardfRepository.createResult(result)

    private suspend fun updateResults(raceId: UUID) {
        getCategoriesForRace(raceId).forEach { category ->
            updateResultsForCategory(category.id, false)
        }
    }

    private suspend fun updateResultsForCategory(categoryId: UUID, delete: Boolean) =
        resultsProcessor?.updateResultsForCategory(categoryId, delete)

    private suspend fun updateResultsForCompetitor(competitorId: UUID) =
        resultsProcessor?.updateResultsForCompetitor(
            competitorId,
            currentState.value?.currentRace!!.id
        )

    //DATA IMPORT/EXPORT
    suspend fun importData(
        uri: Uri,
        dataType: DataType,
        dataFormat: DataFormat,
        raceId: UUID
    ): DataImportWrapper? {
        return fileProcessor?.importData(uri, dataType, dataFormat, getRace(raceId))
    }

    suspend fun exportData(
        uri: Uri,
        dataType: DataType,
        dataFormat: DataFormat,
        raceId: UUID
    ): Boolean {
        return fileProcessor?.exportData(
            uri,
            dataType,
            dataFormat,
            raceId
        ) ?: false
    }


    //SportIdent manipulation
    fun connectDevice(usbDevice: UsbDevice) {
        Intent(appContext.get(), SIReaderService::class.java).also {
            it.action = SIReaderService.ReaderServiceActions.START.toString()
            it.putExtra(SIReaderService.USB_DEVICE, usbDevice)
            appContext.get()?.startService(it)
        }
    }

    fun detachDevice(usbDevice: UsbDevice) {
        Intent(appContext.get(), SIReaderService::class.java).also {
            it.action = SIReaderService.ReaderServiceActions.STOP.toString()
            it.putExtra(SIReaderService.USB_DEVICE, usbDevice)
            appContext.get()?.startService(it)
        }
    }

    fun getLastReadCard(): Int? = currentState.value?.siReaderState?.lastCard

    //PRINTING
    fun enablePrinting() {
        printProcessor.printerReady = true
    }

    //GENERAL HELPER METHODS

    //Enums manipulation
    fun raceTypeToString(raceType: RaceType): String {
        val raceTypeStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_types_array)!!
        return raceTypeStrings[raceType.value]!!
    }

    fun raceTypeStringToEnum(string: String): RaceType {
        val raceTypeStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_types_array)!!
        return RaceType.getByValue(raceTypeStrings.indexOf(string))!!
    }

    fun raceLevelToString(raceLevel: RaceLevel): String {
        val raceLevelStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_levels_array)!!
        return raceLevelStrings[raceLevel.value]
    }

    fun raceLevelStringToEnum(string: String): RaceLevel {
        val raceLevelStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_levels_array)!!
        return RaceLevel.getByValue(raceLevelStrings.indexOf(string))!!
    }

    fun raceBandToString(raceBand: RaceBand): String {
        val raceBandStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_bands_array)!!
        return raceBandStrings[raceBand.value]
    }

    fun raceBandStringToEnum(string: String): RaceBand {
        val raceBandStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_bands_array)!!
        return RaceBand.getByValue(raceBandStrings.indexOf(string))!!
    }

    fun raceStatusToString(raceStatus: RaceStatus): String {
        val raceStatusStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_status_array)!!
        return raceStatusStrings[raceStatus.value]
    }

    fun raceStatusStringToEnum(string: String): RaceStatus {
        val raceStatusStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_status_array)!!
        return RaceStatus.getByValue(raceStatusStrings.indexOf(string))!!
    }

    fun raceStatusToShortString(raceStatus: RaceStatus): String {
        val raceStatusStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_status_array_short)!!
        return raceStatusStrings[raceStatus.value]
    }

    fun startTimeSourceToString(startTimeSource: StartTimeSource): String {
        val startTimeSourceStrings =
            appContext.get()?.resources?.getStringArray(R.array.start_time_sources)!!
        return startTimeSourceStrings[startTimeSource.value]
    }

    fun startTimeSourceStringToEnum(string: String): StartTimeSource {
        val startTimeSourceStrings =
            appContext.get()?.resources?.getStringArray(R.array.start_time_sources)!!
        return StartTimeSource.getByValue(startTimeSourceStrings.indexOf(string))!!
    }

    fun finishTimeSourceToString(finishTimeSource: FinishTimeSource): String {
        val finishTimeSourceStrings =
            appContext.get()?.resources?.getStringArray(R.array.finish_time_sources)!!
        return finishTimeSourceStrings[finishTimeSource.value]
    }

    fun finishTimeSourceStringToEnum(string: String): FinishTimeSource {
        val finishTimeSourceStrings =
            appContext.get()?.resources?.getStringArray(R.array.finish_time_sources)!!
        return FinishTimeSource.getByValue(finishTimeSourceStrings.indexOf(string))!!
    }

    fun genderToString(isMan: Boolean?): String {
        return when (isMan) {
            false -> appContext.get()!!.resources.getString(R.string.gender_woman)
            else -> appContext.get()!!.resources.getString(R.string.gender_man)
        }
    }

    /**
     * @return false for woman, true for man
     */
    fun genderFromString(string: String): Boolean {
        val genderStrings =
            appContext.get()?.resources?.getStringArray(R.array.genders)!!
        return when (genderStrings.indexOf(string)) {
            0 -> false
            1 -> true
            else -> false
        }
    }

    fun dataFormatFromString(string: String): DataFormat {
        val dataStrings = appContext.get()?.resources?.getStringArray(R.array.data_formats)!!
        val index = dataStrings.indexOf(string).or(0)
        return DataFormat.getByValue(index)!!
    }

    fun dataTypeFromString(string: String): DataType {
        val dataStrings = appContext.get()?.resources?.getStringArray(R.array.data_types)!!
        val index = dataStrings.indexOf(string).or(0)
        return DataType.getByValue(index)!!
    }
}