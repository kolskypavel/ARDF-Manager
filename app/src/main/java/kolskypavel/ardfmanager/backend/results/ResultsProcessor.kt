package kolskypavel.ardfmanager.backend.results

import android.content.Context
import android.util.Log
import android.widget.Toast
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.Punch
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SITime
import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.TreeSet
import java.util.UUID


class ResultsProcessor {
    val dataProcessor = DataProcessor.get()

    /**
     * Processes the punches - converts PunchData to Punch entity
     */
    private fun processCardPunches(
        cardData: CardData,
        race: Race,
        readout: Readout, competitorId: UUID?
    ): ArrayList<Punch> {
        val punches = ArrayList<Punch>()

        var orderCounter = 1
        cardData.punchData.forEach { punchData ->
            val punch = Punch(
                UUID.randomUUID(),
                race.id,
                readout.id,
                competitorId,
                cardData.siNumber,
                punchData.siCode,
                punchData.siTime,
                SIRecordType.CONTROL,
                orderCounter,
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
                    race.id,
                    readout.id,
                    competitorId,
                    cardData.siNumber,
                    0,
                    readout.startTime!!,
                    SIRecordType.START,
                    0,
                    PunchStatus.VALID,
                )
            )
        }

        //Add finish punch
        if (readout.finishTime != null) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    race.id,
                    readout.id,
                    competitorId,
                    cardData.siNumber,
                    0,
                    readout.finishTime!!,
                    SIRecordType.FINISH,
                    orderCounter,
                    PunchStatus.VALID,
                )
            )
        }

        calculateSplits(punches)
        return punches
    }

    private fun calculateSplits(punches: ArrayList<Punch>) {
        //Calculate splits
        punches.forEachIndexed { index, punch ->
            if (index != 0) {
                punch.split = SITime.split(punches[index - 1].siTime, punch.siTime)
            }
        }
    }

    /**
     * Processes the given result - saves the data into the db
     */
    suspend fun processCardData(cardData: CardData, race: Race, context: Context): Boolean {

        //Check if readout already exists
        if (!dataProcessor.checkIfReadoutExistsBySI(cardData.siNumber, race.id)) {
            val competitor = dataProcessor.getCompetitorBySINumber(cardData.siNumber, race.id)
            val category = competitor?.categoryId?.let { dataProcessor.getCategory(it) }

            //Create the readout and result
            val readout =
                Readout(
                    UUID.randomUUID(),
                    cardData.siNumber,
                    cardData.cardType,
                    race.id,
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
                    val startTime = TimeProcessor.getAbsoluteDateTimeFromRelativeTime(
                        dataProcessor.getCurrentRace().startDateTime,
                        competitor.drawnRelativeStartTime!!
                    )
                    readout.startTime = SITime(
                        startTime.toLocalTime(), startTime.dayOfWeek.value - 1
                    )
                }
            }

            //Process the punches
            val punches = processCardPunches(
                cardData,
                race,
                readout,
                competitor?.id
            )

            calculateResult(
                readout,
                result,
                category,
                punches,
                null
            )
            return true
        }
        //Duplicate readout
        else {

            //Run on the main UI thread
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    context.getText(R.string.readout_si_exists),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            return false
        }
    }

    suspend fun processManualPunchData(
        readout: Readout,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) {

        var result = dataProcessor.getResultByReadout(readout.id)

        val competitor = if (readout.competitorID != null) {
            dataProcessor.getCompetitor(readout.competitorID!!)
        } else {
            null
        }
        val category = if (competitor?.categoryId != null) {
            dataProcessor.getCategory(competitor.categoryId!!)
        } else {
            null
        }

        //Create new result
        if (result == null) {
            result = Result(
                UUID.randomUUID(),
                readout.id,
                category?.id,
                competitor?.id,
                true,
                RaceStatus.NOT_PROCESSED,
                0,
                Duration.ZERO
            )
        }

        readout.modified = true //Mark the readout punches were modified

        punches.forEachIndexed { order, punch ->
            punch.readoutId = readout.id
            punch.order = order
        }

        //Modify the start and finish times
        if (punches[0].punchType == SIRecordType.START) {
            readout.startTime = punches.first().siTime
        }
        if (punches.last().punchType == SIRecordType.FINISH) {
            readout.finishTime = punches.last().siTime
        }

        calculateResult(
            readout,
            result,
            category,
            punches,
            manualStatus
        )
    }

    private suspend fun calculateResult(
        readout: Readout,
        result: Result,
        category: Category?,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) {
        if (readout.startTime != null && readout.finishTime != null) {
            result.runTime = SITime.split(readout.startTime!!, readout.finishTime!!)
        }

        calculateSplits(punches)
        if (category != null) {
            evaluatePunches(punches, category, result)
        }

        // Set the result status based on user preference
        if (manualStatus != null) {
            result.automaticStatus = false
            result.raceStatus = manualStatus
        } else {
            result.automaticStatus = true
        }
        dataProcessor.saveReadoutAndResult(readout, punches, result)
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

        val raceType = if (category.differentProperties) {
            category.raceType!!
        } else {
            dataProcessor.getCurrentRace().raceType
        }
        when (raceType) {
            RaceType.CLASSICS, RaceType.FOXORING -> evaluateClassics(
                punches,
                controlPoints,
                result
            )

            RaceType.SPRINT -> evaluateSprint(
                punches,
                controlPoints,
                result
            )

            RaceType.ORIENTEERING -> evaluateOrienteering(
                punches,
                controlPoints,
                result
            )

        }
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
                    evaluatePunches(punches, category!!, result!!)
                } else {
                    clearEvaluation(punches, result!!)
                }
                dataProcessor.saveReadoutAndResult(readout, punches, result)
            }
        }
    }

    suspend fun updateResultsForCompetitor(competitorId: UUID, raceId: UUID) {
        var readout = dataProcessor.getReadoutByCompetitor(competitorId)
        val competitor = dataProcessor.getCompetitor(competitorId)

        //Try to get result by SI instead and update competitor ID
        if (readout == null) {
            if (competitor != null) {
                readout =
                    competitor.siNumber?.let { dataProcessor.getReadoutBySINumber(it, raceId) }
            }
            if (readout != null) {
                readout.competitorID = competitorId
            }
        }

        if (readout != null) {
            val result = dataProcessor.getResultByCompetitor(competitorId)
            val punches = ArrayList(dataProcessor.getPunchesByReadout(readout.id))
            if (competitor?.categoryId != null) {
                val category = dataProcessor.getCategory(competitor.categoryId!!)
                evaluatePunches(punches, category!!, result!!)
            } else {
                clearEvaluation(punches, result!!)
            }

            //Save into db
            dataProcessor.saveReadoutAndResult(readout, punches, result)
        }
    }

    private fun List<CompetitorData>.sortByPlace(): List<CompetitorData> {
        val sorted = this.sortedWith(ResultDataComparator())

        var place = 0
        for (cd in sorted.withIndex()) {
            val curr = cd.value.readoutResult

            //Check for first element
            if (cd.index != 0) {
                val prev = sorted[cd.index - 1].readoutResult

                if (curr != null && prev != null
                    && curr.result.runTime == prev.result.runTime
                ) {
                    curr.result.place = place
                } else if (curr != null) {
                    place++
                    curr.result.place = place
                }
            } else if (curr != null) {
                place++
                curr.result.place = place
            }
        }
        return sorted
    }

    private fun List<CompetitorData>.toResultDisplayWrappers(): List<ResultDisplayWrapper> {
        // Transform each ReadoutData item into a ResultDisplayWrapper
        val res = this.groupByCategory().toMutableMap()
        res.forEach { cg ->
            res[cg.key] = cg.value.sortByPlace()
        }

        return res.map { result ->
            ResultDisplayWrapper(
                category = result.key,
                subList = result.value.toMutableList()
            )
        }
    }

    private fun List<CompetitorData>.groupByCategory(): Map<Category?, List<CompetitorData>> {
        return this.groupBy { it.competitorCategory.category }
    }

    fun getResultDataByRace(raceId: UUID): Flow<List<ResultDisplayWrapper>> {
        return dataProcessor.getCompetitorDataFlowByRace(raceId).map { readoutDataList ->
            readoutDataList.toResultDisplayWrappers()
        }
    }

    companion object {
        /**
         * Resets all the punches to unknown, e. g. when the category has been deleted
         */
        fun clearEvaluation(punches: ArrayList<Punch>, result: Result) {
            result.points = 0
            punches.forEach { punch ->
                punch.punchStatus = PunchStatus.UNKNOWN
            }
            result.raceStatus = RaceStatus.NOT_PROCESSED
        }

        /**
         * Process one loop of classics race
         * @return Number of points
         */
        fun evaluateLoop(
            punches: List<Punch>,
            controlPoints: List<ControlPoint>
        ): Int {
            val codes = controlPoints.map { p -> p.siCode }.toSet()
            val taken = TreeSet<Int>()  //Already taken CPs
            var points = 0

            val beacon: Int =
                if (controlPoints.isNotEmpty() && controlPoints.last().type == ControlPointType.BEACON) {
                    controlPoints.last().siCode
                } else -1


            punches.forEach { punch ->
                if (punch.punchType == SIRecordType.CONTROL && codes.contains(punch.siCode)) {

                    //Valid punch
                    if (!taken.contains(punch.siCode) && punch.siCode != beacon) {
                        punch.punchStatus = PunchStatus.VALID
                        points++
                        taken.add(punch.siCode)
                    }
                    //Check if beacon is the last punch
                    else if (punch.siCode == beacon) {
                        if (punches.indexOf(punch) == punches.lastIndex) {
                            points++
                            punch.punchStatus = PunchStatus.VALID
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
            return points
        }

        /**
         * Process the classics race
         */
        fun evaluateClassics(
            punches: ArrayList<Punch>,
            controlPoints: List<ControlPoint>,
            result: Result
        ) {
            result.points = evaluateLoop(punches, controlPoints)

            //Set the status accordingly
            if (result.points > 1) {
                result.raceStatus = RaceStatus.VALID
            } else {
                result.raceStatus = RaceStatus.NO_RANKING
            }
        }

        /**
         * Process the sprint race
         */
        fun evaluateSprint(
            punches: ArrayList<Punch>,
            controlPoints: List<ControlPoint>,
            result: Result
        ) {
            //First is code, second is index
            val separators = ArrayList<Pair<Int, Int>>()
            var points = 0

            //Find separators in the control points
            for (cp in controlPoints.withIndex()) {
                if (cp.value.type == ControlPointType.SEPARATOR) {
                    separators.add(Pair(cp.value.siCode, cp.index))
                }
            }

            if (separators.isNotEmpty()) {
                var prevPunchSep = 0
                var prevControlSep = 0
                var separIndex = 0

                //Find separators in punches and evaluate loops
                for (pun in punches.withIndex()) {
                    if ((separIndex < separators.size &&
                                pun.value.siCode == separators[separIndex].first)
                    ) {
                        points += evaluateLoop(
                            ArrayList(punches.subList(prevPunchSep, pun.index)),
                            controlPoints.subList(
                                prevControlSep,
                                separators[separIndex].second
                            )
                        )
                        prevPunchSep = pun.index
                        prevControlSep = separators[separIndex].second
                        separIndex++
                    }
                }
                //Get last loop
                points += evaluateLoop(
                    punches.subList(prevPunchSep, punches.size),
                    controlPoints.subList(
                        prevControlSep,
                        controlPoints.size
                    )
                )
            }

            //No separator taken
            else {
                points = evaluateLoop(
                    punches,
                    controlPoints
                )    //TODO: Fix the last beacon to not be required
            }

            //Set the status accordingly
            result.points = points
            if (result.points > 1) {
                result.raceStatus = RaceStatus.VALID
            } else {
                result.raceStatus = RaceStatus.NO_RANKING
            }
        }

        /**
         * Process the orienteering race
         */
        fun evaluateOrienteering(
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

        fun adjustControlPoints(
            controlPoints: ArrayList<ControlPoint>,
            raceType: RaceType
        ): List<ControlPoint> {

            var order = 1

            for (cp in controlPoints) {
                cp.order = order

                if (cp.name == null) {
                    cp.name = cp.siCode.toString()
                }
                order++
            }
            return controlPoints.toList()
        }

        fun getCodesNameFromControlPoints(controlPoints: List<ControlPoint>): String {
            var codes = ""

            for (cp in controlPoints) {
                codes += cp.siCode

                if (cp.type == ControlPointType.BEACON) {
                    codes += "B"
                }
                if (cp.type == ControlPointType.SEPARATOR) {
                    codes += "!"
                }
                if (cp.name != null) {
                    codes += " (" + cp.name + "), "

                }
            }
            return codes
        }
    }
}
