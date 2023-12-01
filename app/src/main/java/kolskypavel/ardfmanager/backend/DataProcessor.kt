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
import kolskypavel.ardfmanager.backend.room.enums.EventBand
import kolskypavel.ardfmanager.backend.room.enums.EventLevel
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SIReaderService
import kolskypavel.ardfmanager.backend.sportident.SIReaderState
import kolskypavel.ardfmanager.backend.sportident.SIReaderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
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
    var currentEvent = MutableLiveData<Event>()
    var siReaderState = MutableLiveData<SIReaderState>()
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
        siReaderState.postValue(SIReaderState(SIReaderStatus.DISCONNECTED, null, null, null))
    }

    suspend fun setReaderEvent(eventId: UUID): Event {
        val event = getEvent(eventId)
        currentEvent.postValue(event)

        if (siReaderState.value != null &&
            siReaderState.value!!.status == SIReaderStatus.CONNECTED
        ) {
            siReaderState.postValue(siReaderState.value)
        }

        return event
    }

    fun removeReaderEvent() {
        currentEvent.postValue(null)
        if (siReaderState.value != null) {

            when (siReaderState.value!!.status) {
                SIReaderStatus.CONNECTED -> siReaderState.postValue(siReaderState.value)
                SIReaderStatus.READING,
                SIReaderStatus.ERROR,
                SIReaderStatus.CARD_READ -> {
                    siReaderState.postValue(SIReaderState(SIReaderStatus.CONNECTED))
                }

                else -> {}
            }
        }
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

    fun deleteEvent(id: UUID) {
        runBlocking {
            ardfRepository.deleteEvent(id)
        }
    }

    //CATEGORIES
    fun getCategoriesForEvent(eventId: UUID) = ardfRepository.getCategoriesForEvent(eventId)

    fun getCategory(id: UUID) = ardfRepository.getCategory(id)

    fun createCategory(category: Category, siCodes: String) {
        runBlocking {
            ardfRepository.createCategory(category)
            parseCodeStringIntoControlPoints(siCodes, category.id)
        }
    }

    fun updateCategory(category: Category, siCodes: String) {
        //TODO: Finish the update of category
    }

    fun deleteCategory(id: UUID) {
        runBlocking {
            ardfRepository.deleteCategory(id)
            ardfRepository.deleteControlPointsForCategory(id)
        }
    }

    //COMPETITORS
    fun getCompetitorsForEvent(eventId: UUID) =
        ardfRepository.getCompetitorsForEvent(eventId)

    fun getCompetitor(id: UUID): Competitor = ardfRepository.getCompetitor(id)

    fun getCompetitorBySINumber(siNumber: Int, eventId: UUID): Competitor? =
        ardfRepository.getCompetitorBySINumber(siNumber, eventId)

    fun checkIfSINumberExists(siNumber: Int, eventId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfSINumberExists(siNumber, eventId) > 0
        }
    }

    fun createCompetitor(competitor: Competitor) {
        runBlocking {
            ardfRepository.createCompetitor(competitor)
            //TODO:add punches
        }
    }

    fun updateCompetitor(competitor: Competitor) {
        runBlocking {
            ardfRepository.updateCompetitor(competitor)
            //TODO:update punches
        }
    }

    fun deleteCompetitor(id: UUID) {
        runBlocking {
            ardfRepository.deleteCompetitor(id)
            //TODO:delete punches
        }
    }

    //READOUTS

    fun getReadoutsByEvent(eventId: UUID) = ardfRepository.getReadoutsForEvent(eventId)

    fun getReadout(id: UUID) = ardfRepository.getReadout(id)

    fun getReadoutBySINumber(siNumber: Int, eventId: UUID): Readout? =
        ardfRepository.getReadoutBySINumber(siNumber, eventId)

    fun createReadout(readout: Readout) = ardfRepository.createReadout(readout)

    fun checkIfReadoutExistsBySI(siNumber: Int, eventId: UUID): Boolean {
        return runBlocking {
            return@runBlocking ardfRepository.checkIfReadoutExistsById(siNumber, eventId) > 0
        }
    }

    //PUNCHES
    fun createPunch(punch: Punch) = ardfRepository.createPunch(punch)

    fun processCardData(cardData: CardData, event: Event) =
        appContext.get()?.let { resultsProcessor?.processCardData(cardData, event, it) }

    suspend fun getPunchesForCompetitor(competitorId: UUID) =
        ardfRepository.getPunchesForCompetitor(competitorId)

    //Parsing categories to control points
    fun checkCodesString(string: String): Boolean {

        val regex = Regex("(\\b\\d+(?:-\\d+)?[!b]?\\s*)*")
        return regex.matches(string)
    }

    private fun parseCodeStringIntoControlPoints(siCodes: String, categoryId: UUID) {

        //Handle empty CP situation
        if (siCodes.isEmpty()) {
            return
        }
        //Replace multiple whitespaces with one and trim the spaces
        val replaced = siCodes.replace("\\s+".toRegex(), " ").trim()

        val regex = Regex("(\\b\\d+(?:-\\d+)?[!b]?\\s*)*")
        val match = regex.find(replaced)

        val controlPoints = mutableListOf<ControlPoint>()

        var order = 0
        var round = 0
        var siText: String
        var points = 1

        val orig = match?.value.toString().split(' ')

        orig.forEach { cp ->
            if (cp.contains("-")) {
                siText = cp.substringBefore("-")
                points = cp.substringAfter("-", "").toInt()
            } else {
                siText = cp
            }

            val beacon = siText.endsWith("b", true)
            val separator = siText.endsWith("!")

            //Get the code
            if (beacon || separator) {
                siText = siText.dropLast(1)
            }

            val controlPoint = ControlPoint(
                UUID.randomUUID(),
                categoryId,
                siText.toInt(),
                order,
                round,
                points,
                beacon,
                separator
            )
            controlPoints.add(controlPoint)

            if (separator) {
                round++
            }
            order++
        }

        //TODO save the CPs to database
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


    //GENERAL HELPER METHODS
    fun getHoursMinutesFromTime(time: LocalTime): String {
        return DateTimeFormatter.ofPattern("HH:mm").format(time).toString()
    }

    //Enums manipulation
    fun eventTypeToString(eventType: EventType): String {
        val eventTypeStrings = appContext.get()?.resources?.getStringArray(R.array.event_types)!!
        return eventTypeStrings[eventType.value]!!
    }

    fun eventTypeStringToEnum(string: String): EventType {
        val eventTypeStrings = appContext.get()?.resources?.getStringArray(R.array.event_types)!!
        return EventType.getByValue(eventTypeStrings.indexOf(string))!!
    }

    fun eventLevelToString(eventLevel: EventLevel): String {
        val eventLevelStrings = appContext.get()?.resources?.getStringArray(R.array.event_levels)!!
        return eventLevelStrings[eventLevel.value]!!
    }

    fun eventLevelStringToEnum(string: String): EventLevel {
        val eventLevelStrings = appContext.get()?.resources?.getStringArray(R.array.event_levels)!!
        return EventLevel.getByValue(eventLevelStrings.indexOf(string))!!
    }

    fun eventBandToString(eventBand: EventBand): String {
        val eventBandStrings = appContext.get()?.resources?.getStringArray(R.array.event_bands)!!
        return eventBandStrings[eventBand.value]!!
    }

    fun eventBandStringToEnum(string: String): EventBand {
        val eventBandStrings = appContext.get()?.resources?.getStringArray(R.array.event_bands)!!
        return EventBand.getByValue(eventBandStrings.indexOf(string))!!
    }
}