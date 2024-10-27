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
import kolskypavel.ardfmanager.backend.room.entitity.Result
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceStatus
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SIConstants
import kolskypavel.ardfmanager.backend.sportident.SIPort.CardData
import kolskypavel.ardfmanager.backend.sportident.SITime
import kolskypavel.ardfmanager.backend.wrappers.ResultWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.TreeSet
import java.util.UUID


class ResultsProcessor(
    private val dataProcessor: DataProcessor = DataProcessor.get()
) {
    fun adjustTime(previous: SITime, current: SITime): SITime {
        if (current.isAtOrAfter(previous)) {
            return current
        }

        val cmp = SITime(current)
        cmp.addHalfDay()

        if (cmp.isAtOrAfter(previous)) {
            return cmp
        }

        current.addDay()
        return current
    }

    /**
     * Adjust the times for the SI_CARD5, because it operates on 12h mode instead of 24h
     */
    fun card5TimeAdjust(result: Result, punches: List<Punch>, zeroTimeBase: LocalTime) {
        //Solve start and check
        if (result.startTime != null) {
            result.startTime = adjustTime(SITime(zeroTimeBase), result.origStartTime!!)
        }

        //Adjust the punches
        for (punch in punches.withIndex()) {

            val previousTime = if (punch.index == 0) {
                if (result.startTime != null) {
                    result.startTime!!
                } else {
                    SITime(zeroTimeBase)
                }
            } else {
                punches[punch.index - 1].siTime
            }

            val currentTime = punch.value.siTime
            currentTime.setDayOfWeek(previousTime.getDayOfWeek())
            currentTime.setWeek(previousTime.getWeek())
            punches[punch.index].siTime = adjustTime(previousTime, currentTime)
        }

        if (result.finishTime != null) {

            val prefinishTime = if (punches.isEmpty()) {
                if (result.startTime != null) {
                    result.startTime!!
                } else {
                    SITime(zeroTimeBase)
                }
            } else {
                punches.last().siTime
            }

            val finishTime = result.finishTime!!
            finishTime.setDayOfWeek(prefinishTime.getDayOfWeek())
            finishTime.setWeek(prefinishTime.getWeek())

            result.finishTime = adjustTime(prefinishTime, finishTime)
        }
    }

    /**
     * Processes the punches - converts PunchData to Punch entity
     */
    fun processCardPunches(
        cardData: CardData,
        raceId: UUID,
        result: Result,
        zeroTimeBase: LocalTime,
        competitorId: UUID?
    ): ArrayList<Punch> {
        val punches = ArrayList<Punch>()

        var orderCounter = 1
        cardData.punchData.forEach { punchData ->
            val punch = Punch(
                UUID.randomUUID(),
                raceId,
                result.id,
                cardData.siNumber,
                punchData.siCode,
                punchData.siTime,
                punchData.siTime,
                SIRecordType.CONTROL,
                orderCounter,
                PunchStatus.UNKNOWN,
                Duration.ZERO
            )
            punches.add(punch)
            orderCounter++
        }

        if (cardData.cardType == SIConstants.SI_CARD5) {
            card5TimeAdjust(result, punches, zeroTimeBase)
        }
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

        //Check if result already exists
        if (dataProcessor.getResultBySINumber(cardData.siNumber, race.id) != null) {
            val competitor = dataProcessor.getCompetitorBySINumber(cardData.siNumber, race.id)
            val category = competitor?.categoryId?.let { dataProcessor.getCategory(it) }

            //Create the result
            val result =
                Result(
                    UUID.randomUUID(),
                    race.id,
                    competitor?.id,
                    category?.id,
                    cardData.siNumber,
                    cardData.cardType,
                    cardData.checkTime,
                    cardData.checkTime,
                    cardData.startTime,
                    cardData.startTime,
                    cardData.finishTime,
                    cardData.finishTime,
                    LocalDateTime.now(),
                    false,
                    RaceStatus.NO_RANKING,
                    0,
                    Duration.ZERO,
                    false
                )


            //TODO: Based on options, set start time to the predefined value
            if (competitor != null) {
                if (result.startTime == null && competitor.drawnRelativeStartTime != null) {
                    val startTime = TimeProcessor.getAbsoluteDateTimeFromRelativeTime(
                        dataProcessor.getCurrentRace().startDateTime,
                        competitor.drawnRelativeStartTime!!
                    )
                    result.startTime = SITime(
                        startTime.toLocalTime(), startTime.dayOfWeek.value - 1
                    )
                }
            }

            //Process the punches
            val punches = processCardPunches(
                cardData,
                race.id,
                result,
                dataProcessor.getCurrentRace().startDateTime.toLocalTime(),
                competitor?.id
            )

            calculateResult(
                result,
                category,
                punches,
                null
            )
            return true
        }
        //Duplicate result
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
        result: Result,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) {
        val competitor = if (result.competitorID != null) {
            dataProcessor.getCompetitor(result.competitorID!!)
        } else {
            null
        }
        val category = if (competitor?.categoryId != null) {
            dataProcessor.getCategory(competitor.categoryId!!)
        } else {
            null
        }

        result.modified = true //Mark the result punches were modified

        punches.forEachIndexed { order, punch ->
            punch.resultId = result.id
            punch.order = order
        }

        //Modify the start and finish times
        if (punches.first().punchType == SIRecordType.START) {
            result.startTime = punches.first().siTime
            punches.removeFirst()
        }
        if (punches.last().punchType == SIRecordType.FINISH) {
            result.finishTime = punches.last().siTime
            punches.removeLast()
        }

        calculateResult(
            result,
            category,
            punches,
            manualStatus
        )
    }

    private suspend fun calculateResult(
        result: Result,
        category: Category?,
        punches: ArrayList<Punch>,
        manualStatus: RaceStatus?
    ) {
        if (result.startTime != null && result.finishTime != null) {
            result.runTime = SITime.split(result.startTime!!, result.finishTime!!)
        }

        if (category != null) {
            evaluatePunches(punches, category, result)
        }

        val originalStartTime = result.startTime
        val originalFinishTime = result.finishTime

        if (result.cardType == SIConstants.SI_CARD5) {
            val zeroTimeBase = dataProcessor.getCurrentRace().startDateTime.toLocalTime()
            card5TimeAdjust(result, punches, zeroTimeBase)
        }

        // Add back start and finish
        if (result.startTime != null) {

            punches.add(
                0,
                Punch(
                    UUID.randomUUID(),
                    result.raceId,
                    result.id,
                    result.siNumber,
                    0,
                    result.startTime!!,
                    originalStartTime!!,
                    SIRecordType.START,
                    0,
                    PunchStatus.VALID,
                    Duration.ZERO
                )
            )
        }

        //Add finish punch
        if (result.finishTime != null) {
            punches.add(
                Punch(
                    UUID.randomUUID(),
                    result.raceId,
                    result.id,
                    result.siNumber,
                    0,
                    result.finishTime!!,
                    originalFinishTime!!,
                    SIRecordType.FINISH,
                    punches.size,
                    PunchStatus.VALID,
                    Duration.ZERO
                )
            )
        }

        calculateSplits(punches)

        // Set the result status based on user preference
        if (manualStatus != null) {
            result.automaticStatus = false
            result.raceStatus = manualStatus
        } else {
            result.automaticStatus = true
        }
        dataProcessor.saveResultPunches(result, punches)
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

            val result = dataProcessor.getResultByCompetitor(competitor.id)
            result?.let {
                val punches = ArrayList(dataProcessor.getPunchesByResult(result.id))

                if (!delete) {
                    val category = dataProcessor.getCategory(categoryId)
                    evaluatePunches(punches, category!!, result)
                } else {
                    clearEvaluation(punches, result)
                }
                dataProcessor.saveResultPunches(result, punches)
            }
        }
    }

    suspend fun updateResultsForCompetitor(competitorId: UUID, raceId: UUID) {
        var result = dataProcessor.getResultByCompetitor(competitorId)
        val competitor = dataProcessor.getCompetitor(competitorId)

        //Try to get result by SI instead and update competitor ID
        if (result != null) {
            val punches = ArrayList(dataProcessor.getPunchesByResult(result.id))
            val category = competitor?.categoryId?.let { dataProcessor.getCategory(it) }

            if (category == null) {
                clearEvaluation(punches, result)
            }
            calculateResult(result, category, punches, null)
        }
    }

    private fun List<CompetitorData>.sortByPlace(): List<CompetitorData> {
        val sorted = this.sortedWith(ResultDataComparator())

        var place = 0
        for (cd in sorted.withIndex()) {
            val curr = cd.value.resultData

            //Check for first element
            if (cd.index != 0) {
                val prev = sorted[cd.index - 1].resultData

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

    private fun List<CompetitorData>.toResultDisplayWrappers(): List<ResultWrapper> {
        // Transform each ReadoutData item into a ResultDisplayWrapper
        val res = this.groupByCategory().toMutableMap()
        res.forEach { cg ->
            res[cg.key] = cg.value.sortByPlace()
        }

        return res.map { result ->
            ResultWrapper(
                category = result.key,
                subList = result.value.toMutableList()
            )
        }
    }

    private fun List<CompetitorData>.groupByCategory(): Map<Category?, List<CompetitorData>> {
        return this.groupBy { it.competitorCategory.category }
    }

    fun getResultWrappersByRace(raceId: UUID): Flow<List<ResultWrapper>> {
        return dataProcessor.getCompetitorDataFlowByRace(raceId).map { resultDataList ->
            resultDataList.toResultDisplayWrappers()
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
            result.raceStatus = RaceStatus.NO_RANKING
        }

        /**
         * Process one loop of classics race
         * @return Number of points
         */
        private fun evaluateLoop(
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
    }
}
