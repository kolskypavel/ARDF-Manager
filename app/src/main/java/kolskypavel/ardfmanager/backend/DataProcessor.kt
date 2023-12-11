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
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.enums.EventBand
import kolskypavel.ardfmanager.backend.room.enums.EventLevel
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SIReaderService
import kolskypavel.ardfmanager.backend.sportident.SIReaderState
import kolskypavel.ardfmanager.backend.sportident.SIReaderStatus
import kolskypavel.ardfmanager.backend.sportident.SITime
import kolskypavel.ardfmanager.backend.wrappers.PunchWrapper
import kolskypavel.ardfmanager.backend.wrappers.ReadoutDataWrapper
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

    suspend fun setReaderEvent(eventId: UUID): Event {
        val event = getEvent(eventId)
        currentState.postValue(currentState.value?.let { AppState(event, it.siReaderState) })

        return event
    }

    fun removeReaderEvent() {
        currentState.postValue(currentState.value?.let { AppState(null, it.siReaderState) })
    }

    //METHODS TO HANDLE EVENTS
    suspend fun getEvents(): Flow<List<Event>> = ardfRepository.getEvents()

    suspend fun getEvent(id: UUID): Event = ardfRepository.getEvent(id)

    fun createEvent(
        event: Event
    ) {
        runBlocking {
            ardfRepository.createEvent(event)
        }
    }

    fun modifyEvent(
        event: Event
    ) {
        runBlocking {
            ardfRepository.updateEvent(event)
        }
    }

    suspend fun deleteEvent(id: UUID) {
        ardfRepository.deleteEvent(id)
        ardfRepository.deleteCompetitorsByEvent(id)
        ardfRepository.deleteCategoriesByEvent(id)
        ardfRepository.deleteControlPointsByEvent(id)
        ardfRepository.deleteReadoutsByEvent(id)
        ardfRepository.deletePunchesByEvent(id)
    }

    //CATEGORIES
    fun getCategoriesForEvent(eventId: UUID) = ardfRepository.getCategoriesForEvent(eventId)

    suspend fun getCategory(id: UUID) = ardfRepository.getCategory(id)

    suspend fun createCategory(category: Category) {
        runBlocking {
            ardfRepository.createCategory(category)
            createControlPoints(category.siCodes, category)
        }
    }

    suspend fun updateCategory(category: Category) {
        ardfRepository.deleteControlPointsByCategory(category.id)
        ardfRepository.updateCategory(category)
        createControlPoints(category.siCodes, category)
        updateReadoutsForCategory(category.id)
    }

    suspend fun deleteCategory(id: UUID) {
        ardfRepository.deleteCategory(id)
        ardfRepository.deleteControlPointsByCategory(id)
        updateReadoutsForCategory(id)
    }

    //CONTROL POINTS
    suspend fun getControlPointsByCategory(categoryId: UUID) =
        ardfRepository.getControlPointsByCategory(categoryId)

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

    suspend fun createCompetitor(competitor: Competitor) {
        ardfRepository.createCompetitor(competitor)
    }

    suspend fun updateCompetitor(competitor: Competitor) {
        ardfRepository.updateCompetitor(competitor)
        updateReadoutsForCompetitor(competitor.id)
    }


    suspend fun deleteCompetitor(id: UUID) {
        ardfRepository.deleteCompetitor(id)
        updateReadoutsForCompetitor(id)
    }

    //READOUTS

    suspend fun getReadoutDataByEvent(eventId: UUID): Flow<List<ReadoutDataWrapper>> {
        return flow {
            while (true) {
                val temp = ArrayList<ReadoutDataWrapper>()

                ardfRepository.getReadoutsByEvent(eventId).forEach { readout ->

                    val punches = getPunchesForSICard(readout.siNumber, eventId)
                    val competitor = getCompetitorBySINumber(readout.siNumber, eventId)
                    val punchWrappers = ArrayList<PunchWrapper>()

                    //Add start
                    if (readout.startTime != null) {
                        punchWrappers.add(
                            PunchWrapper(
                                null, null,
                                null,
                                readout.startTime!!,
                                PunchStatus.VALID,
                                null, SIRecordType.START
                            )
                        )
                    }
                    //Add control points
                    punches.forEach { punch ->
                        punchWrappers.add(
                            PunchWrapper(
                                punch.cardNumber,
                                punch.siCode,
                                punch.order,
                                punch.siTime,
                                punch.punchStatus,
                                punch.split,
                                SIRecordType.CONTROL
                            )
                        )
                    }
                    if (readout.finishTime != null) {
                        val finishSplit = if (punches.isNotEmpty()) {
                            SITime.split(punches.last().siTime, readout.finishTime!!)
                        } else {
                            null
                        }
                        punchWrappers.add(
                            PunchWrapper(
                                null, null,
                                null,
                                readout.finishTime!!,
                                PunchStatus.VALID,
                                finishSplit, SIRecordType.FINISH
                            )
                        )
                    }

                    var category: Category? = null
                    if (competitor?.categoryId != null) {
                        category = getCategory(competitor.categoryId!!)
                    }
                    temp.add(
                        ReadoutDataWrapper(
                            readout,
                            punchWrappers,
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

    suspend fun getReadoutBySINumber(siNumber: Int, eventId: UUID): Readout? =
        ardfRepository.getReadoutBySINumber(siNumber, eventId)

    suspend fun getReadoutByCompetitor(competitorId: UUID): Readout? =
        ardfRepository.getReadoutByCompetitor(competitorId)

    suspend fun createReadout(readout: Readout) = ardfRepository.createReadout(readout)

    fun checkIfReadoutExistsBySI(siNumber: Int, eventId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfReadoutExistsById(siNumber, eventId) > 0
        }
    }

    private suspend fun updateReadoutsForCategory(categoryId: UUID) =
        resultsProcessor?.updateReadoutsForCategory(categoryId)

    private suspend fun updateReadoutsForCompetitor(competitorId: UUID) =
        resultsProcessor?.updateReadoutsForCompetitor(competitorId)


    suspend fun deleteReadout(id: UUID) {
        ardfRepository.deleteReadout(id)
        ardfRepository.deletePunchesByReadoutId(id)
    }

    //PUNCHES
    private suspend fun createPunch(punch: Punch) = ardfRepository.createPunch(punch)

    suspend fun createPunches(punches: ArrayList<Punch>) {
        punches.forEach { punch -> createPunch(punch) }
    }

    suspend fun processCardData(cardData: CardData, event: Event) =
        appContext.get()?.let { resultsProcessor?.processCardData(cardData, event, it) }


    suspend fun getPunchesForCompetitor(competitorId: UUID) =
        ardfRepository.getPunchesByCompetitor(competitorId)

    private suspend fun getPunchesForSICard(siNumber: Int, eventId: UUID) =
        ardfRepository.getPunchesBySINumber(siNumber, eventId)


    //Parsing categories to control points
    fun checkCodesString(string: String, eventType: EventType) =
        ResultsProcessor.checkCodesString(string, eventType)

    private suspend fun createControlPoints(
        siCodes: String,
        category: Category
    ) {
        val processed =
            ResultsProcessor.parseIntoControlPoints(siCodes, category.id, category.eventId)
        processed?.forEach { cp -> ardfRepository.createControlPoint(cp) }
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