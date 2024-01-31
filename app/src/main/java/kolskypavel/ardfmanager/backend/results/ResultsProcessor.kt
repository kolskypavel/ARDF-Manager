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
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.enums.EventType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SITime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.TreeSet
import java.util.UUID


class ResultsProcessor {
    val dataProcessor = DataProcessor.get()

    private suspend fun saveResult(result: Result, punches: ArrayList<Punch>) {
        dataProcessor.createResult(result)
        dataProcessor.createPunches(punches)
    }

    /**
     * Processes the given result - saves the data into the db
     */
    suspend fun processCardData(cardData: CardData, event: Event, context: Context): Boolean {

        //Check if result already exists
        if (!dataProcessor.checkIfResultExistsBySI(cardData.siNumber, event.id)) {
            val competitor = dataProcessor.getCompetitorBySINumber(cardData.siNumber, event.id)
            val category = competitor?.categoryId?.let { dataProcessor.getCategory(it) }

            //Create the result
            val result =
                Result(
                    UUID.randomUUID(),
                    cardData.siNumber,
                    cardData.cardType,
                    event.id,
                    competitor?.id,
                    null,
                    cardData.checkTime,
                    cardData.startTime,
                    cardData.finishTime,
                    Duration.ZERO,
                    LocalDateTime.now(),
                    RaceStatus.NOT_PROCESSED,
                    0
                )

            //TODO: Based on options, set start time to the predefined value
            if (competitor != null) {
                if (result.startTime == null && competitor.defaultStartTime != null) {
                    val startTime =
                        SITime(competitor.defaultStartTime!!, event.date.dayOfWeek.value - 1)

                    result.startTime = startTime
                }
            }

            //Process the punches
            val punches = processCardPunches(
                cardData,
                event,
                result,
                competitor?.id,
                category
            )

            //Adjust run time
            if (result.startTime != null && result.finishTime != null) {
                result.runTime = SITime.split(result.startTime!!, result.finishTime!!)
            } else {
                result.raceStatus = RaceStatus.ERROR
                Log.d("Results processor", "Missing finish or start time")
            }

            //Save to db
            saveResult(result, punches)
            return true
        }
        //Duplicate readout
        else {

            //Run on the main UI thread
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, context.getText(R.string.readout_exists), Toast.LENGTH_LONG)
                    .show()
            }
            return false
        }
    }

    suspend fun processManualPunches(
        competitorId: UUID,
        categoryId: UUID?,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) {
        var result = dataProcessor.getResultByCompetitor(competitorId)

        //New result, just the data is updated
        if (result == null) {

            result = Result(
                UUID.randomUUID(),
                null,
                0,
                dataProcessor.getCurrentEvent().id,
                competitorId,
                categoryId,
                null,
                null,
                null,
                Duration.ZERO,
                LocalDateTime.now(), RaceStatus.NOT_PROCESSED, 0
            )
        }
        //Result exists - delete the old punches
        else {
            dataProcessor.deletePunchesForResult(result.id)
        }
        if (punches.isNotEmpty()) {

            var startPunch: Punch? = null
            var finishPunch: Punch? = null

            //Modify the start and finish times
            if (punches[0].punchType == SIRecordType.START) {
                result.startTime = punches[0].siTime
                startPunch = punches[0]
                punches.removeFirst()
            }
            if (punches.last().punchType == SIRecordType.FINISH) {
                result.finishTime = punches.last().siTime
                finishPunch = punches.last()
                punches.removeLast()
            }

            //Process punches without start and finish
            if (categoryId != null) {
                val category = dataProcessor.getCategory(categoryId)
                evaluatePunches(punches, category, result)
            }

            //Add back start and finish
            punches.add(0, startPunch!!)
            punches.add(finishPunch!!)
            result.runTime = SITime.split(startPunch.siTime!!, finishPunch.siTime!!)

            var index = 0
            //Calculate splits
            punches.forEach { punch ->
                if (index != 0) {
                    punch.split =
                        SITime.split(punches[index - 1].siTime!!, punch.siTime!!)
                }
                punch.order = index
                punch.resultId = result.id
                index++
            }

            // Set the result status based on user preference
            if (manualStatus != null) {
                result.raceStatus = manualStatus
            }
            saveResult(result, punches)
        }
    }

    /**
     * Processes the punches - converts PunchData to Punch entity
     */
    private suspend fun processCardPunches(
        cardData: CardData,
        event: Event,
        result: Result, competitorId: UUID?, category: Category?,
    ): ArrayList<Punch> {
        val punches = ArrayList<Punch>()

        var orderCounter = 1
        cardData.punchData.forEach { punchData ->
            val punch = Punch(
                UUID.randomUUID(),
                event.id,
                result.id,
                competitorId,
                cardData.siNumber,
                SIRecordType.CONTROL,
                punchData.siCode,
                orderCounter,
                punchData.siTime,
                punchData.siTime,
                PunchStatus.UNKNOWN,
            )
            punches.add(punch)
            orderCounter++
        }

        //Evaluate without start and finish
        if (category != null) {
            evaluatePunches(punches, category, result)
        }

        //Add start punch
        if (result.startTime != null) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    event.id,
                    result.id,
                    competitorId,
                    cardData.siNumber,
                    SIRecordType.START,
                    0,
                    0,
                    result.startTime!!,
                    result.startTime!!,
                    PunchStatus.VALID,
                )
            )
        }

        //Add finish punch
        if (result.finishTime != null) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    event.id,
                    result.id,
                    competitorId,
                    cardData.siNumber,
                    SIRecordType.FINISH,
                    0,
                    orderCounter,
                    result.finishTime!!,
                    result.finishTime!!,
                    PunchStatus.VALID,
                )
            )
        }

        //Calculate splits
        punches.forEach { punch ->
            if (punch.order != 0) {
                punch.split = SITime.split(punches[punch.order - 1].siTime!!, punch.siTime!!)
            }
        }
        return punches
    }

    private suspend fun evaluatePunches(
        punches: ArrayList<Punch>,
        category: Category, result: Result
    ) {

        var controlPoints: List<ControlPoint> = ArrayList()
        try {
            controlPoints = dataProcessor.getControlPointsByCategory(category.id)
        } catch (e: Exception) {
            Log.d("ResultsProcess", e.message.toString())
        }
        result.points = 0
        when (category.eventType) {
            EventType.CLASSICS, EventType.FOXORING -> processClassics(
                punches,
                controlPoints,
                result
            )

            EventType.SPRINT -> processSprint(
                punches,
                controlPoints,
                result
            )

            EventType.ORIENTEERING -> processOrienteering(
                punches,
                controlPoints,
                result
            )

            EventType.CUSTOM -> processCustom(
                punches,
                controlPoints,
                result
            )
        }
    }

    /**
     * Resets all the punches to unknown, e. g. when the category has been deleted
     */
    private fun clearEvaluation(punches: ArrayList<Punch>, result: Result) {
        result.points = 0
        punches.forEach { punch ->
            punch.punchStatus = PunchStatus.UNKNOWN
        }
        result.raceStatus = RaceStatus.NOT_PROCESSED
    }

    /**
     * Process the classics race
     */
    private fun processClassics(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        result: Result
    ) {
        val codes = TreeSet<Int>()
        var beacon: Int = -1

        controlPoints.forEach { cp ->
            if (!cp.beacon) {
                codes.add(cp.siCode!!)
            } else {
                beacon = cp.siCode!!
            }
        }
        val taken = TreeSet<Int>()  //Already taken CPs

        punches.forEach { punch ->
            if (punch.punchType == SIRecordType.CONTROL && codes.contains(punch.siCode)) {

                //Valid punch
                if (!taken.contains(punch.siCode) && punch.siCode != beacon) {
                    punch.punchStatus = PunchStatus.VALID
                    result.points++
                    taken.add(punch.siCode)
                }
                //Check if beacon is the last punch
                else if (punch.siCode == beacon) {
                    if (punch.order == punches.size) {
                        result.points++

                    } else {
                        punch.punchStatus = PunchStatus.INVALID
                    }
                }
                //Duplicate punch
                else {
                    punch.punchStatus = PunchStatus.DUPLICATE
                }
            }
            //Unknown punch
            else {
                punch.punchStatus = PunchStatus.UNKNOWN
            }
        }

        //Set the status accordingly
        if (result.points > 1) {
            result.raceStatus = RaceStatus.VALID
        } else {
            result.raceStatus = RaceStatus.NOT_PROCESSED
        }
    }

    /**
     * Process the sprint race
     */
    private fun processSprint(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        result: Result
    ) {
        //Find separator in the control points
        var separator: Int? = null
        var pos = 0

        for (cp in controlPoints) {
            if (cp.separator) {
                separator = pos
                break
            }
            pos++
        }

        //If separator was found, process both loops
        if (separator != null) {
            var firstRound = controlPoints.subList(0, separator)
            var secondRound = controlPoints.subList(separator, controlPoints.size - 1)

            for (punch in punches) {

            }
        }
    }

    /**
     * Process the orienteering race
     */
    private fun processOrienteering(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        result: Result
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
                result.points++
            }
            //Break in a loop
            else {
                punch.punchStatus = PunchStatus.INVALID
            }
        }

        if (result.points == controlPoints.size) {
            result.raceStatus = RaceStatus.VALID
        } else {
            result.raceStatus = RaceStatus.DISQUALIFIED
        }
    }

    /**
     * Process the custom race
     */
    private fun processCustom(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        result: Result
    ) {
        //TODO: Not yet implemented
    }

    /**
     * Updates the already read out data in case of a change in category / competitor
     */
    suspend fun updateResultsForCategory(categoryId: UUID, delete: Boolean) {
        val competitors = dataProcessor.getCompetitorsByCategory(categoryId)

        competitors.forEach { competitor ->

            //Update competitor
            if (delete) {
                competitor.categoryId = null
                dataProcessor.updateCompetitor(competitor)
            }

            val result = dataProcessor.getResultByCompetitor(competitor.id)
            result?.let {
                val punches = ArrayList(dataProcessor.getPunchesByResult(result.id))

                if (!delete) {
                    val category = dataProcessor.getCategory(categoryId)
                    evaluatePunches(punches, category, result)
                } else {
                    clearEvaluation(punches, result)
                }

                saveResult(result, punches)
            }
        }
    }

    suspend fun updateResultsForCompetitor(competitorId: UUID, eventId: UUID, delete: Boolean) {
        var result = dataProcessor.getResultByCompetitor(competitorId)
        val competitor = dataProcessor.getCompetitor(competitorId)

        //Try to get result by SI instead and update competitor ID
        if (result == null && !delete) {
            result = competitor.siNumber?.let { dataProcessor.getResultBySINumber(it, eventId) }
            if (result != null) {
                result.competitorID = competitorId
            }
        }
        if (result != null) {
            val punches = ArrayList(dataProcessor.getPunchesByResult(result.id))
            if (!delete && competitor.categoryId != null) {
                val category = dataProcessor.getCategory(competitor.categoryId!!)
                evaluatePunches(punches, category, result)
            } else {
                clearEvaluation(punches, result)
            }

            //Save into db
            saveResult(result, punches)
        }
    }

    companion object {

//        fun parseIntoControlPoints(
//            siCodes: String,
//            categoryId: UUID,
//            eventId: UUID
//        ): ArrayList<ControlPoint>? {
//
//            //Handle empty CP situation
//            if (siCodes.isEmpty()) {
//                return null
//            }
//            //Replace multiple whitespaces with one and trim the spaces
//            val replaced = siCodes.replace("\\s+".toRegex(), " ").trim()
//
//            val regex = Regex("(\\b\\d+(?:-\\d+)?[!b]?\\s*)*")
//            val match = regex.find(replaced)
//
//            val controlPoints = ArrayList<ControlPoint>()
//
//            var order = 0
//            var round = 0
//            var siText: String
//            var points = 1
//
//            val orig = match?.value.toString().split(' ')
//
//            orig.forEach { cp ->
//                if (cp.contains("-")) {
//                    siText = cp.substringBefore("-")
//                    points = cp.substringAfter("-", "").toInt()
//                } else {
//                    siText = cp
//                }
//
//                val beacon = siText.endsWith("b", true)
//                val separator = siText.endsWith("!")
//
//                //Get the code
//                if (beacon || separator) {
//                    siText = siText.dropLast(1)
//                }
//
//                val controlPoint = ControlPoint(
//                    UUID.randomUUID(),
//                    eventId, categoryId,
//                    siText.toInt(),
//                    order,
//                    round,
//                    points,
//                    beacon,
//                    separator
//                )
//                controlPoints.add(controlPoint)
//
//                if (separator) {
//                    round++
//                }
//                order++
//            }
//            return controlPoints
//        }
    }
}