package kolskypavel.ardfmanager.backend.files.json.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.json.temps.PunchJson
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import java.util.UUID

/** Moshi adapter for converting punch records to JSON control/split rows. */
class PunchJsonAdapter(val raceId: UUID, val dataProcessor: DataProcessor) {

    /** Serializes a punch using an alias display name when one exists. */
    @ToJson
    fun toJson(aliasPunch: AliasPunch): PunchJson {
        val punch = aliasPunch.punch
        return PunchJson(
            code = aliasPunch.alias?.name ?: punch.siCode.toString(),
            si_code = punch.siCode,
            control_type = punch.punchType.name,
            punch_status = dataProcessor.punchStatusToShortString(punch.punchStatus),
            split_time = TimeProcessor.durationToFormattedString(punch.split, true)
        )
    }

    /** Deserializes a JSON punch and leaves absolute SI time reconstruction to the result adapter. */
    @FromJson
    fun fromJson(punchJson: PunchJson): Punch {
        val punchType = SIRecordType.valueOf(punchJson.control_type)
        return Punch(
            id = UUID.randomUUID(),
            raceId = raceId,
            resultId = UUID.randomUUID(),
            cardNumber = 0,
            siCode = if (punchType == SIRecordType.CONTROL) {
                if (punchJson.si_code != null) {
                    punchJson.si_code!!
                } else punchJson.code.toInt()
            } else 0, // START and FINISH punches do not carry a control SI code.

            siTime = SITime(),
            origSiTime = SITime(),
            punchType = punchType,
            order = 0,
            punchStatus = dataProcessor
                .shortStringToPunchStatus(punchJson.punch_status),
            split = TimeProcessor.minuteStringToDuration(punchJson.split_time),
        )
    }
}
