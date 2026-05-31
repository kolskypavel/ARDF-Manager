package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import org.openardf.radioomanager.shared.files.EventCsvRows
import org.openardf.radioomanager.shared.files.TimedPunchCsvField
import java.io.Serializable

/** Room aggregate for a readout, its punches, and the matched competitor/category when available. */
data class ResultData(
    @Embedded var result: Result,

    @Relation(
        entityColumn = "result_id",
        parentColumn = "id",
        entity = Punch::class
    )
    var punches: List<AliasPunch>,
    @Relation(
        parentColumn = "competitor_id",
        entityColumn = "id",
        entity = Competitor::class
    ) var competitorCategory: CompetitorCategory?

) : Serializable {
    /** Returns only the punch entities from the alias-punch relation list. */
    fun getPunchList(): List<Punch> {
        return punches.map { p -> p.punch }
    }

    /** Formats this readout in the legacy readout CSV export shape. */
    fun toReadoutCSVString(): String {
        val controlPunches = punches
            .filter { punch -> punch.punch.punchType == SIRecordType.CONTROL }
            .map { punch ->
                TimedPunchCsvField(
                    siCode = punch.punch.siCode,
                    timeText = punch.punch.siTime.getTimeString()
                )
            }

        return EventCsvRows.readoutRow(
            siNumber = result.siNumber,
            checkTimeText = result.checkTime?.getTimeString(),
            startTimeText = result.startTime?.getTimeString(),
            finishTimeText = result.finishTime?.getTimeString(),
            controlPunches = controlPunches
        )
    }
}
