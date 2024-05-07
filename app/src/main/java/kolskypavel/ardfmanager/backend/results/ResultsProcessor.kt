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


    /**
     * Processes the given result - saves the data into the db
     */
    suspend fun processCardData(cardData: CardData, event: Event, context: Context): Boolean {

        //Check if readout already exists
        if (!dataProcessor.checkIfReadoutExistsBySI(cardData.siNumber, event.id)) {
            val competitor = dataProcessor.getCompetitorBySINumber(cardData.siNumber, event.id)
            val category = competitor?.categoryId?.let { dataProcessor.getCategory(it) }

            //Create the readout and result
            val readout =
                Readout(
                    UUID.randomUUID(),
                    cardData.siNumber,
                    cardData.cardType,
                    event.id,
                    competitor?.id,
                    cardData.checkTime,
                    cardData.startTime,
                    cardData.finishTime,
                    LocalDateTime.now(),
                    false
                )

            val result = Result(
                UUID.randomUUID(),
                readout.id,
                category?.id,
                competitor?.id,
                true,
                RaceStatus.NOT_PROCESSED,
                0,
                Duration.ZERO
            )

            //TODO: Based on options, set start time to the predefined value
            if (competitor != null) {
                if (readout.startTime == null && competitor.drawnRelativeStartTime != null) {
//                    val startTime =
//                        SITime(TimeProcessor.getAbsoluteDateTimeFromRelativeTime(competitor.drawnRelativeStartTime!!), event.date.dayOfWeek.value - 1)
//
//                    readout.startTime = startTime
                }
            }

            //Process the punches
            val punches = processCardPunches(
                cardData,
                event,
                readout,
                competitor?.id
            )

            //Adjust run time
            if (readout.startTime != null && readout.finishTime != null) {
                //  readout.runTime = SITime.split(readout.startTime!!, readout.finishTime!!)
            } else {
                Log.d("Results processor", "Missing finish or start time")
            }

            if (category != null) {
                evaluatePunches(punches, category, result)
            }

            //Save to db
            dataProcessor.saveReadoutAndResult(readout, punches, result)
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
        var readout = dataProcessor.getReadoutByCompetitor(competitorId)
        var result = dataProcessor.getResultByCompetitor(competitorId)

        //New readout
        if (readout == null) {

            readout = Readout(
                UUID.randomUUID(),
                null,
                0,
                dataProcessor.getCurrentEvent().id,
                competitorId,
                null,
                null,
                null,
                LocalDateTime.now(),
                true
            )

            result = Result(
                UUID.randomUUID(),
                readout.id,
                categoryId,
                competitorId,
                true,
                RaceStatus.NOT_PROCESSED,
                0,
                Duration.ZERO
            )
        }
        readout.modified = true //Mark the readout punches were modified

        if (punches.isNotEmpty()) {
            //Modify the start and finish times
            if (punches[0].punchType == SIRecordType.START) {
                readout.startTime = punches[0].siTime
            }
            if (punches.last().punchType == SIRecordType.FINISH) {
                readout.finishTime = punches.last().siTime
            }

            if (categoryId != null) {
                val category = dataProcessor.getCategory(categoryId)
                evaluatePunches(punches, category, result!!)
            }

            calculateSplits(punches)

            // Set the result status based on user preference
            if (manualStatus != null) {
                result!!.automaticStatus = false
                result.raceStatus = manualStatus
            } else {
                result!!.automaticStatus = true
            }
            dataProcessor.saveReadoutAndResult(readout, punches, result)
        }
    }

    /**
     * Processes the punches - converts PunchData to Punch entity
     */
    private fun processCardPunches(
        cardData: CardData,
        event: Event,
        readout: Readout, competitorId: UUID?
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

        //Add start punch
        if (readout.startTime != null) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    event.id,
                    readout.id,
                    competitorId,
                    cardData.siNumber,
                    SIRecordType.START,
                    0,
                    0,
                    readout.startTime!!,
                    readout.startTime!!,
                    PunchStatus.VALID,
                )
            )
        }

        //Add finish punch
        if (readout.finishTime != null) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    event.id,
                    readout.id,
                    competitorId,
                    cardData.siNumber,
                    SIRecordType.FINISH,
                    0,
                    orderCounter,
                    readout.finishTime!!,
                    readout.finishTime!!,
                    PunchStatus.VALID,
                )
            )
        }

        calculateSplits(punches)
        return punches
    }

    private fun calculateSplits(punches: ArrayList<Punch>) {
        //Calculate splits
        punches.forEach { punch ->
            if (punch.order != 0) {
                punch.split = SITime.split(punches[punch.order - 1].siTime, punch.siTime)
            }
        }
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

        //Decies
        val eventType = if (category.differentProperties) {
            category.eventType!!
        } else {
            dataProcessor.getCurrentEvent().eventType
        }
        when (eventType) {
            EventType.CLASSICS, EventType.FOXORING -> evaluateClassics(
                punches,
                controlPoints,
                result
            )

            EventType.SPRINT -> evaluateSprint(
                punches,
                controlPoints,
                result
            )

            EventType.ORIENTEERING -> evaluateOrienteering(
                punches,
                controlPoints,
                result
            )

            EventType.CUSTOM -> evaluateCustom(
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
    private fun evaluateClassics(
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
    private fun evaluateSprint(
        punches: ArrayList<Punch>,
        controlPoints: List<ControlPoint>,
        result: Result
    ) {
        //Find separators in the control points
        val separators = ArrayList<Int>()

        for ((pos, cp) in controlPoints.withIndex()) {
            if (cp.separator) {
                separators.add(pos)
            }
        }

        //Process the loops
        for (pos in separators) {

        }
    }

    /**
     * Process the orienteering race
     */
    private fun evaluateOrienteering(
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
    private fun evaluateCustom(
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

            val readout = dataProcessor.getReadoutByCompetitor(competitor.id)
            val result = dataProcessor.getResultByCompetitor(competitor.id)
            readout?.let {
                val punches = ArrayList(dataProcessor.getPunchesByReadout(readout.id))

                if (!delete) {
                    val category = dataProcessor.getCategory(categoryId)
                    evaluatePunches(punches, category, result!!)
                } else {
                    clearEvaluation(punches, result!!)
                }
                dataProcessor.saveReadoutAndResult(readout, punches, result)
            }
        }
    }

    suspend fun updateResultsForCompetitor(competitorId: UUID, eventId: UUID) {
        var readout = dataProcessor.getReadoutByCompetitor(competitorId)
        val competitor = dataProcessor.getCompetitor(competitorId)

        //Try to get result by SI instead and update competitor ID
        if (readout == null) {
            readout = competitor.siNumber?.let { dataProcessor.getReadoutBySINumber(it, eventId) }
            if (readout != null) {
                readout.competitorID = competitorId
            }
        }

        if (readout != null) {
            val result = dataProcessor.getResultByCompetitor(competitorId)
            val punches = ArrayList(dataProcessor.getPunchesByReadout(readout.id))
            if (competitor.categoryId != null) {
                val category = dataProcessor.getCategory(competitor.categoryId!!)
                evaluatePunches(punches, category, result!!)
            } else {
                clearEvaluation(punches, result!!)
            }

            //Save into db
            dataProcessor.saveReadoutAndResult(readout, punches, result)
        }
    }

    companion object {

        fun adjustControlPoints(
            controlPoints: ArrayList<ControlPoint>,
            eventType: EventType
        ): List<ControlPoint> {

            var order = 1
            var round = 1

            for (cp in controlPoints) {
                cp.order = order
                cp.round = round

                if (cp.separator) {
                    round++
                }

                if (cp.name == null) {
                    cp.name = cp.siCode.toString()
                }
                order++
            }
            return controlPoints.toList()
        }
    }

    fun getCodesNameFromControlPoints(controlPoints: List<ControlPoint>): Pair<String, String> {
        var names = ""
        var codes = ""

        for (cp in controlPoints) {
            codes += cp.siCode

            if (cp.beacon) {
                codes += "B"
            }
            if (cp.separator) {
                codes += "!"
            }
            if (cp.name != null) {
                codes += " (" + cp.name + "), "
                names += cp.name + " "
            } else {
                names += cp.siCode
                names += " "
            }
        }
        return Pair(names, codes)
    }
}
