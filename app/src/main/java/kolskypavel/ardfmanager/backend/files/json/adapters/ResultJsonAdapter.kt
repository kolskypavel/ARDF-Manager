import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.json.adapters.PunchJsonAdapter
import kolskypavel.ardfmanager.backend.files.json.temps.ResultJson
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/** Moshi adapter for converting matched competitor readouts to result JSON. */
class ResultJsonAdapter(
    val race: Race,
    val dataProcessor: DataProcessor
) {
    val punchJsonAdapter = PunchJsonAdapter(race.id, dataProcessor)

    /** Serializes a matched readout and excludes the synthetic start punch from split output. */
    @ToJson
    fun toJson(resultData: CompetitorData): ResultJson {
        val result = resultData.readoutData?.result!!
        val punches =
            resultData.readoutData!!.punches.filter { it.punch.punchType != SIRecordType.START }

        return ResultJson(
            check_time = result.checkTime?.toLocalDateTime(race.startDateTime),
            start_time = result.startTime!!.toLocalDateTime(race.startDateTime),
            finish_time = result.finishTime!!.toLocalDateTime(race.startDateTime),
            modified = result.modified,
            run_time = TimeProcessor.durationToFormattedString(result.runTime, true),
            place = result.place,
            punch_count = result.points,
            result_status = dataProcessor
                .resultStatusToShortString(result.resultStatus),
            automatic_status = result.automaticStatus,
            punches = punches
                .map { ap ->
                    val rawCode = ap.alias?.name ?: ap.punch.siCode.toString()
                    val code =
                        if (ap.punch.punchType == SIRecordType.FINISH && rawCode == "0") "F" else rawCode
                    punchJsonAdapter.toJson(ap).also { it.code = code }

                },
            readoutTime = result.readoutTime
        )
    }

    /** Deserializes a matched readout and reconstructs absolute punch times from split durations. */
    @Suppress("DEPRECATION")
    @FromJson
    fun fromJson(resultJson: ResultJson): ReadoutData {

        val result = Result(
            id = UUID.randomUUID(),
            raceId = race.id,
            siNumber = null, // Assigned by CompetitorJsonAdapter after the competitor is created.
            cardType = 0, // Result JSON does not store SportIdent card type.
            checkTime = resultJson.check_time?.let { SITime(it, race.startDateTime) },
            points = resultJson.punch_count ?: 0,
            startTime = resultJson.start_time?.let { SITime(it, race.startDateTime) },
            finishTime = resultJson.finish_time?.let {
                resultJson.start_time?.let { startZero ->
                    SITime(
                        it,
                        startZero
                    )
                }
            },
            automaticStatus = resultJson.automatic_status ?: true,
            resultStatus = dataProcessor.resultStatusShortStringToEnum(resultJson.result_status),
            runTime = resultJson.run_time?.let { TimeProcessor.minuteStringToDuration(resultJson.run_time) }
                ?: Duration.ZERO,
            modified = resultJson.modified ?: false,
            sent = false,
            readoutTime = resultJson.readoutTime ?: LocalDateTime.now()
        )

        val punches = ArrayList<AliasPunch>()
        val punchJsonAdapter = PunchJsonAdapter(race.id, dataProcessor)
        val prevTime = SITime(result.startTime ?: SITime(race.startDateTime.toLocalTime()))

        resultJson.punches.forEachIndexed { index, punchJson ->

            val punch = punchJsonAdapter.fromJson(punchJson)
            punch.order = index
            punch.resultId = result.id

            prevTime.addTime(punch.split)
            punch.siTime = SITime(prevTime)

            punches.add(
                AliasPunch(punch, null)
            )
        }

        return ReadoutData(result, punches)
    }
}
