package kolskypavel.ardfmanager.backend

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import androidx.lifecycle.MutableLiveData
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.ARDFRepository
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.enums.EventBand
import kolskypavel.ardfmanager.backend.room.enums.EventLevel
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SIReaderService
import kolskypavel.ardfmanager.backend.sportident.SIReaderState
import kolskypavel.ardfmanager.backend.sportident.SIReaderStatus
import kolskypavel.ardfmanager.backend.wrappers.ReadoutDataWrapper
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID


/**
 * This is the main backend interface, processing and providing various sources of data
 */
class DataProcessor private constructor(context: Context) {

    private val ardfRepository = ARDFRepository.get()
    private var appContext: WeakReference<Context>

    //private val siReaderService: SIReaderService
    var currentState = MutableLiveData<AppState>()
    var resultsProcessor: ResultsProcessor? = null

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
        appContext = WeakReference(context)
        currentState.postValue(
            AppState(null, SIReaderState(SIReaderStatus.DISCONNECTED))
        )
    }

    fun updateReaderState(newSIState: SIReaderState) {
        val stateToUpdate = currentState.value

        if (stateToUpdate != null) {
            stateToUpdate.siReaderState = newSIState
            currentState.postValue(stateToUpdate!!)
        }
    }

    suspend fun setCurrentEvent(eventId: UUID): Event {
        val event = getEvent(eventId)
        currentState.postValue(currentState.value?.let { AppState(event, it.siReaderState) })

        return event
    }

    fun getCurrentEvent() = currentState.value?.currentEvent!!

    fun removeReaderEvent() {
        currentState.postValue(currentState.value?.let { AppState(null, it.siReaderState) })
    }

    //METHODS TO HANDLE EVENTS
    suspend fun getEvents(): Flow<List<Event>> = ardfRepository.getEvents()

    private suspend fun getEvent(id: UUID): Event = ardfRepository.getEvent(id)

    fun createEvent(
        event: Event
    ) {
        runBlocking {
            ardfRepository.createEvent(event)
        }
    }

    fun updateEvent(
        event: Event
    ) {
        runBlocking {
            ardfRepository.updateEvent(event)
        }
    }

    suspend fun deleteEvent(id: UUID) {
        ardfRepository.deleteEvent(id)
    }

    //CATEGORIES
    fun getCategoriesForEvent(eventId: UUID) = ardfRepository.getCategoriesFlowForEvent(eventId)

    suspend fun getCategory(id: UUID) = ardfRepository.getCategory(id)

    suspend fun createCategory(category: Category, controlPoints: List<ControlPoint>) {
        runBlocking {
            ardfRepository.createCategory(category)
            createControlPoints(controlPoints)
        }
    }

    suspend fun updateCategory(category: Category, controlPoints: List<ControlPoint>) {
        ardfRepository.deleteControlPointsByCategory(category.id)
        ardfRepository.updateCategory(category)
        createControlPoints(controlPoints)
        updateResultsForCategory(category.id, false)
    }

    suspend fun deleteCategory(id: UUID) {
        ardfRepository.deleteCategory(id)
        ardfRepository.deleteControlPointsByCategory(id)
        updateResultsForCategory(id, true)
    }

    //CONTROL POINTS
    suspend fun getControlPointsByCategory(categoryId: UUID) =
        ardfRepository.getControlPointsByCategory(categoryId)

    fun adjustControlPoints(
        controlPoints: ArrayList<ControlPoint>,
        eventType: EventType
    ) = ResultsProcessor.adjustControlPoints(controlPoints, eventType)

    private suspend fun createControlPoints(controlPoints: List<ControlPoint>) {
        controlPoints.forEach { cp ->
            ardfRepository.createControlPoint(cp)
        }
    }

    suspend fun getControlPointByName(eventId: UUID, name: String) =
        ardfRepository.getControlPointByName(eventId, name)


    fun getCodesNameFromControlPoints(controlPoints: List<ControlPoint>): Pair<String, String> =
        resultsProcessor!!.getCodesNameFromControlPoints(controlPoints)


    //COMPETITORS
    fun getCompetitorsForEvent(eventId: UUID) =
        ardfRepository.getCompetitorsByEvent(eventId)


    suspend fun getCompetitor(id: UUID): Competitor = ardfRepository.getCompetitor(id)

    suspend fun getCompetitorBySINumber(siNumber: Int, eventId: UUID): Competitor? =
        ardfRepository.getCompetitorBySINumber(siNumber, eventId)

    suspend fun getCompetitorsByCategory(categoryId: UUID): List<Competitor> =
        ardfRepository.getCompetitorsByCategory(categoryId)

    fun checkIfSINumberExists(siNumber: Int, eventId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfSINumberExists(siNumber, eventId) > 0
        }
    }

    suspend fun createOrUpdateCompetitor(
        competitor: Competitor,
        modifiedPunches: Boolean,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) {
        ardfRepository.createCompetitor(competitor)

        //If punches were added, process and save them
        if (modifiedPunches) {
            resultsProcessor?.processManualPunches(
                competitor.id,
                competitor.categoryId,
                punches,
                manualStatus
            )
        }
        updateResultsForCompetitor(competitor.id, false)
    }

    suspend fun updateCompetitor(competitor: Competitor) =
        ardfRepository.updateCompetitor(competitor)

    suspend fun deleteCompetitor(id: UUID) {
        ardfRepository.deleteCompetitor(id)
        updateResultsForCompetitor(id, true)
    }


    //READOUTS

    suspend fun getReadoutDataByEvent(eventId: UUID): Flow<List<ReadoutDataWrapper>> {
        return flow {
            while (true) {
                val temp = ArrayList<ReadoutDataWrapper>()

                ardfRepository.getReadoutsByEvent(eventId).forEach { result ->

                    val competitor =
                        if (result.competitorID != null) {
                            getCompetitor(result.competitorID!!)
                        } else if (result.siNumber != null) {
                            getCompetitorBySINumber(result.siNumber!!, eventId)
                        } else null


                    var category: Category? = null
                    if (competitor?.categoryId != null) {
                        category = getCategory(competitor.categoryId!!)
                    }
                    temp.add(
                        ReadoutDataWrapper(
                            result,
                            competitor,
                            category
                        )
                    )
                }

                emit(temp)
                delay(1000) //TODO: FIX
            }
        }
    }

    suspend fun getResultDataByEvent(eventId: UUID): Flow<List<ResultDisplayWrapper>> {
        return flow {
            while (true) {

                val temp = ArrayList<ResultDisplayWrapper>()

                ardfRepository.getCategoriesForEvent(eventId).forEach { category ->
                    val res = ResultDisplayWrapper(category)
                    res.subList = ArrayList()
                    ardfRepository.getReadoutsByCategory(category.id).forEach { result ->

                        val competitor =
                            if (result.competitorID != null) {
                                getCompetitor(result.competitorID!!)
                            } else if (result.siNumber != null) {
                                getCompetitorBySINumber(result.siNumber!!, eventId)
                            } else null

                        res.subList.add(ReadoutDataWrapper(result, competitor, category))
                    }
                    temp.add(res)
                }

                //Add competitors with no category
                val noCatRes = ResultDisplayWrapper()
                noCatRes.subList = ArrayList()
                val results = ardfRepository.getReadoutsForNullCategory(eventId)

                if (results.isNotEmpty()) {

                    results.forEach { result ->
                        val competitor =
                            if (result.competitorID != null) {
                                getCompetitor(result.competitorID!!)
                            } else if (result.siNumber != null) {
                                getCompetitorBySINumber(result.siNumber!!, eventId)
                            } else null

                        noCatRes.subList.add(ReadoutDataWrapper(result, competitor, null))
                    }
                    temp.add(noCatRes)
                }

                emit(temp)
                delay(1000) //TODO: FIX

            }
        }
    }

    suspend fun getReadoutBySINumber(siNumber: Int, eventId: UUID): Readout? =
        ardfRepository.getReadoutBySINumber(siNumber, eventId)

    suspend fun getReadoutByCompetitor(competitorId: UUID): Readout? =
        ardfRepository.getReadoutsByCompetitor(competitorId)

    suspend fun createReadout(readout: Readout) = ardfRepository.createReadout(readout)

    fun checkIfReadoutExistsBySI(siNumber: Int, eventId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfReadoutExistsById(siNumber, eventId) > 0
        }
    }

    suspend fun deleteReadout(id: UUID) {
        ardfRepository.deleteReadout(id)
        ardfRepository.deletePunchesByReadoutId(id)
    }

    //PUNCHES
    suspend fun getPunchesByReadout(resultId: UUID) =
        ardfRepository.getPunchesByReadout(resultId)

    suspend fun getPunchesByCompetitor(competitorId: UUID) =
        ardfRepository.getPunchesByCompetitor(competitorId)

    private suspend fun createPunch(punch: Punch) = ardfRepository.createPunch(punch)

    suspend fun createPunches(punches: ArrayList<Punch>) {
        punches.forEach { punch -> createPunch(punch) }
    }

    suspend fun processCardData(cardData: CardData, event: Event) =
        appContext.get()?.let { resultsProcessor?.processCardData(cardData, event, it) }


    suspend fun deletePunchesForReadout(resultId: UUID) =
        ardfRepository.deletePunchesByReadoutId(resultId)


    //RESULTS

    suspend fun getResultByCompetitor(competitorId: UUID) =
        ardfRepository.getResultByCompetitor(competitorId)

    suspend fun createResult(result: Result) = ardfRepository.createResult(result)
    private suspend fun updateResultsForCategory(categoryId: UUID, delete: Boolean) =
        resultsProcessor?.updateResultsForCategory(categoryId, delete)

    private suspend fun updateResultsForCompetitor(competitorId: UUID, delete: Boolean) =
        resultsProcessor?.updateResultsForCompetitor(
            competitorId,
            currentState.value?.currentEvent!!.id,
            delete
        )


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

    //GENERAL HELPER METHODS
    fun getHoursMinutesFromTime(time: LocalTime): String {
        return DateTimeFormatter.ofPattern("HH:mm").format(time).toString()
    }

    //Enums manipulation
    fun eventTypeToString(eventType: EventType): String {
        val eventTypeStrings =
            appContext.get()?.resources?.getStringArray(R.array.event_types_array)!!
        return eventTypeStrings[eventType.value]!!
    }

    fun eventTypeStringToEnum(string: String): EventType {
        val eventTypeStrings =
            appContext.get()?.resources?.getStringArray(R.array.event_types_array)!!
        return EventType.getByValue(eventTypeStrings.indexOf(string))!!
    }

    fun eventLevelToString(eventLevel: EventLevel): String {
        val eventLevelStrings =
            appContext.get()?.resources?.getStringArray(R.array.event_levels_array)!!
        return eventLevelStrings[eventLevel.value]
    }

    fun eventLevelStringToEnum(string: String): EventLevel {
        val eventLevelStrings =
            appContext.get()?.resources?.getStringArray(R.array.event_levels_array)!!
        return EventLevel.getByValue(eventLevelStrings.indexOf(string))!!
    }

    fun eventBandToString(eventBand: EventBand): String {
        val eventBandStrings =
            appContext.get()?.resources?.getStringArray(R.array.event_bands_array)!!
        return eventBandStrings[eventBand.value]
    }

    fun eventBandStringToEnum(string: String): EventBand {
        val eventBandStrings =
            appContext.get()?.resources?.getStringArray(R.array.event_bands_array)!!
        return EventBand.getByValue(eventBandStrings.indexOf(string))!!
    }

    fun raceStatusToString(raceStatus: RaceStatus): String {
        val raceStatusStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_status_array)!!
        return raceStatusStrings[raceStatus.value]
    }

    fun raceStatusToShortString(raceStatus: RaceStatus): String {
        val raceStatusStrings =
            appContext.get()?.resources?.getStringArray(R.array.race_status_array_short)!!
        return raceStatusStrings[raceStatus.value]
    }


    fun durationToString(duration: Duration): String {
        val seconds = duration.seconds
        return if (kotlin.math.abs(seconds / 60) <= 99) {
            String.format("%02d:%02d", (seconds % 3600) / 60, kotlin.math.abs(seconds) % 60);
        } else if (kotlin.math.abs(seconds / 60) <= 999) {
            String.format("%03d:%02d", (seconds % 3600) / 60, kotlin.math.abs(seconds) % 60)
        } else {
            String.format("%04d:%02d", (seconds % 3600) / 60, kotlin.math.abs(seconds) % 60)
        }
    }
}