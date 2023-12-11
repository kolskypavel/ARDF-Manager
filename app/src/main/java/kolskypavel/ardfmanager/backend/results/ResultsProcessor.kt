package kolskypavel.ardfmanager.backend.results

import android.content.Context
import android.util.Log
import android.widget.Toast
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Event
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SITime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.TreeSet
import java.util.UUID


class ResultsProcessor {
    val dataProcessor = DataProcessor.get()

    /**
     * Processes the given readout - saves the data into the db
     */
    suspend fun processCardData(cardData: CardData, event: Event, context: Context): Boolean {

        //Check if readout already exists
        if (!dataProcessor.checkIfReadoutExistsBySI(cardData.siNumber, event.id)) {
            val competitor = dataProcessor.getCompetitorBySINumber(cardData.siNumber, event.id)
            val category = competitor?.categoryId?.let { dataProcessor.getCategory(it) }

            val runTime =
                if (cardData.startTime != null && cardData.finishTime != null) {
                    SITime.split(cardData.startTime!!, cardData.finishTime!!)
                } else {
                    null
                }

            //Save the readout
            val readout =
                Readout(
                    UUID.randomUUID(),
                    cardData.siNumber,
                    cardData.cardType,
                    event.id,
                    competitor?.id,
                    cardData.checkTime, cardData.startTime, cardData.finishTime,
                    runTime, LocalDateTime.now(), RaceStatus.NOT_EVALUATED, 0
                )

            //Process the punches
            val punches = processPunches(
                cardData,
                event,
                readout,
                competitor?.id,
                category
            )

            //Save to db
            dataProcessor.createReadout(readout)
            dataProcessor.createPunches(punches)

            return true
        } else {

            //Run on the main UI thread
            CoroutineScope(Dispatchers.Main).launch {
                //TODO: Notify with snackMessage about existing readout
                Toast.makeText(context, context.getText(R.string.readout_exists), Toast.LENGTH_LONG)
                    .show()

            }
            return false
        }
    }

    /**
     * Processes the punches - converts PunchData to Punch entity
     */
    private suspend fun processPunches(
        cardData: CardData,
        event: Event,
        readout: Readout, competitorId: UUID?, category: Category?,
    ): ArrayList<Punch> {
        val punches = ArrayList<Punch>()

        var orderCounter = 1
        cardData.punchData.forEach { punchData ->
            val punch = Punch(
                UUID.randomUUID(),
                event.id,
                readout.id,
                competitorId,
                cardData.siNumber,
                punchData.siCode, orderCounter, punchData.siTime, PunchStatus.UNKNOWN,
            )
            punches.add(punch)
            orderCounter++
        }

        //Calculate splits
        punches.forEach { punch ->

            //First punch
            if (punch.order == 1 && readout.startTime != null) {
                punch.split = SITime.split(readout.startTime!!, punch.siTime)
            }

            //Further punches
            else if (punch.order != 1) {
                punch.split = SITime.split(punches[punch.order - 2].siTime, punch.siTime)
            }
        }

        if (category != null) {
            evaluatePunches(punches, category, readout)
        }
        return punches
    }

    private suspend fun evaluatePunches(
        punches: ArrayList<Punch>,
        category: Category, readout: Readout
    ) {

        var controlPoints: List<ControlPoint> = ArrayList()
        try {
            controlPoints = dataProcessor.getControlPointsByCategory(category.id)
        } catch (e: Exception) {
            Log.d("ResultsProcess", e.message.toString())
        }
        readout.points = 0
        when (category.eventType) {
            EventType.CLASSICS, EventType.FOXORING -> processClassics(
                punches,
                controlPoints,
                readout
            )

            EventType.SPRINT -> processSprint(
                punches,
                controlPoints,
                readout
            )

            EventType.ORIENTEERING -> processOrienteering(
                punches,
                controlPoints,
                readout
            )

            EventType.CUSTOM -> processCustom(
                punches,
                controlPoints,
                readout
            )
        }
    }

    /**
     * Process the classics race
     */
    private fun processClassics(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        readout: Readout
    ) {
        val codes = TreeSet<Int>()
        var beacon: Int = -1

        controlPoints.forEach { cp ->
            if (!cp.beacon) {
                codes.add(cp.siCode)
            } else {
                beacon = cp.siCode
            }
        }
        val taken = TreeSet<Int>()  //Already taken CPs

        punches.forEach { punch ->
            if (codes.contains(punch.siCode)) {

                //Valid punch
                if (!taken.contains(punch.siCode)) {
                    punch.punchStatus = PunchStatus.VALID
                    readout.points++
                    taken.add(punch.siCode)
                }
                //Duplicate
                else {
                    punch.punchStatus = PunchStatus.DUPLICATE
                }
            }
            //Invalid
            else {
                punch.punchStatus = PunchStatus.UNKNOWN
            }
        }

        //TODO: Check beacon


        //Set the status accordingly
        if (readout.points > 1) {
            readout.raceStatus = RaceStatus.VALID
        } else {
            readout.raceStatus = RaceStatus.NOT_EVALUATED
        }
    }

    /**
     * Process the sprint race
     */
    private fun processSprint(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        readout: Readout
    ) {

    }

    /**
     * Process the orienteering race
     */
    private fun processOrienteering(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        readout: Readout
    ) {
        var cpIndex = 0

        //TODO: Inform about missing punches
        for (punch in punches) {
            //Check bounds
            if (cpIndex > controlPoints.size) {
                break
            }

            if (punch.siCode == controlPoints[cpIndex].siCode) {
                cpIndex++
                punch.punchStatus = PunchStatus.VALID
                readout.points++
            }
            //Break in a loop
            else {
                punch.punchStatus = PunchStatus.INVALID
            }
        }

        if (readout.points == controlPoints.size) {
            readout.raceStatus = RaceStatus.VALID
        } else {
            readout.raceStatus = RaceStatus.DISQUALIFIED
        }
    }

    /**
     * Process the custom race
     */
    private fun processCustom(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        readout: Readout
    ) {
        //TODO: Not yet implemented
    }

    /**
     * Updates the already read out data in case of a change in category / competitor
     */
    suspend fun updateReadoutsForCategory(categoryId: UUID) {
        //Get the category and the corresponding competitors
        val category = dataProcessor.getCategory(categoryId)
        val competitors = dataProcessor.getCompetitorsByCategory(categoryId)

        competitors.forEach { competitor ->
            val punches = ArrayList(dataProcessor.getPunchesForCompetitor(competitor.id))
            val readout = dataProcessor.getReadoutByCompetitor(competitor.id)
            if (readout != null) {
                evaluatePunches(punches, category, readout)
                dataProcessor.createPunches(punches)
                dataProcessor.createReadout(readout)
            }
        }
    }

    suspend fun updateReadoutsForCompetitor(competitorId: UUID) {}

    companion object {

        /**
         * Checks if the provided codesString corresponds with the category definition
         */
        @Throws(IllegalArgumentException::class)
        fun checkCodesString(codesString: String, eventType: EventType): Boolean {
            return when (eventType) {
                EventType.CLASSICS, EventType.FOXORING -> checkClassics(codesString)
                EventType.SPRINT -> checkSprint(codesString)
                EventType.ORIENTEERING -> checkOrienteering(codesString)
                EventType.CUSTOM -> checkCustom(codesString)
            }
        }

        private fun checkClassics(codesString: String): Boolean {
            val check = Regex("((\\b\\d+(?!B)\\b\\s+)*(\\b\\d+B\\b))?")

            if (codesString.matches(check)) {
                return true
            }
            return false
        }

        private fun checkSprint(codesString: String): Boolean {
            return false
        }

        private fun checkOrienteering(codesString: String): Boolean {
            val check = Regex("(\\b\\d+\\b\\s*)*")
            if (codesString.matches(check)) {
                return true
            }
            return false
        }

        private fun checkCustom(codesString: String): Boolean {
            return false
        }

        fun parseIntoControlPoints(
            siCodes: String,
            categoryId: UUID,
            eventId: UUID
        ): ArrayList<ControlPoint>? {

            //Handle empty CP situation
            if (siCodes.isEmpty()) {
                return null
            }
            //Replace multiple whitespaces with one and trim the spaces
            val replaced = siCodes.replace("\\s+".toRegex(), " ").trim()

            val regex = Regex("(\\b\\d+(?:-\\d+)?[!b]?\\s*)*")
            val match = regex.find(replaced)

            val controlPoints = ArrayList<ControlPoint>()

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
                    eventId, categoryId,
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
            return controlPoints
        }
    }
}