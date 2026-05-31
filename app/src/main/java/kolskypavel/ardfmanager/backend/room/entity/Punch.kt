package kolskypavel.ardfmanager.backend.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kolskypavel.ardfmanager.backend.room.enums.PunchStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.sportident.SITime
import org.openardf.radioomanager.shared.files.EventCsvRows
import java.io.Serializable
import java.time.Duration
import java.util.UUID

/** Room entity for one punch read from a SportIdent card. */
@Entity(
    tableName = "punch", foreignKeys = [ForeignKey(
        entity = Result::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("result_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Punch(
    @PrimaryKey var id: UUID,
    @ColumnInfo(name = "race_id") var raceId: UUID,
    @ColumnInfo(name = "result_id") var resultId: UUID?,
    @ColumnInfo(name = "card_number") var cardNumber: Int? = null,
    @ColumnInfo(name = "si_code") var siCode: Int,
    @ColumnInfo(name = "si_time") var siTime: SITime,
    @ColumnInfo(name = "orig_si_time") var origSiTime: SITime,
    @ColumnInfo(name = "punch_type") var punchType: SIRecordType,
    @ColumnInfo(name = "order") var order: Int,
    @ColumnInfo(name = "punch_status") var punchStatus: PunchStatus,
    @ColumnInfo(name = "split") var split: Duration
) : Serializable {
    /** Formats this punch in the legacy readout CSV row shape. */
    fun toCsvString(): String {
        return EventCsvRows.punchRow(cardNumber, siCode, siTime.toString())
    }

    /** Default constructor used by debug views, tests, and tooling that require defaults. */
    constructor() : this(
        id = UUID.randomUUID(),
        raceId = UUID.randomUUID(),
        resultId = null,
        cardNumber = null,
        siCode = 0,
        siTime = SITime(),
        origSiTime = SITime(),
        punchType = SIRecordType.CONTROL,
        order = 0,
        punchStatus = PunchStatus.UNKNOWN,
        split = Duration.ZERO
    )

    /** Convenience constructor for building parsed SportIdent punch records. */
    constructor(siCode: Int, siTime: SITime, punchType: SIRecordType, order: Int) : this(
        id = UUID.randomUUID(),
        raceId = UUID.randomUUID(),
        resultId = null,
        cardNumber = null,
        siCode = siCode,
        siTime = siTime,
        origSiTime = siTime,
        punchType = punchType,
        order = order,
        punchStatus = PunchStatus.UNKNOWN,
        split = Duration.ZERO
    )
}
