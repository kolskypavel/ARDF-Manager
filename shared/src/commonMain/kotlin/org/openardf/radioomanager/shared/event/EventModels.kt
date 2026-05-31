package org.openardf.radioomanager.shared.event

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.PunchStatus
import org.openardf.radioomanager.shared.domain.RaceBand
import org.openardf.radioomanager.shared.domain.RaceLevel
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.domain.ResultStatus
import org.openardf.radioomanager.shared.domain.SIRecordType

/** Portable race metadata used by shared services and non-Android clients. */
data class EventRace(
    val id: String,
    val name: String,
    val apiKey: String,
    val startDateTimeIso: String,
    val raceType: RaceType,
    val raceLevel: RaceLevel,
    val raceBand: RaceBand,
    val timeLimitSeconds: Long
)

/** Portable category definition, including optional category-level race overrides. */
data class EventCategory(
    val id: String,
    val raceId: String,
    val name: String,
    val isMan: Boolean,
    val maxAge: Int?,
    val lengthMeters: Int,
    val climbMeters: Int,
    val order: Int,
    val differentProperties: Boolean,
    val raceType: RaceType?,
    val raceBand: RaceBand?,
    val timeLimitSeconds: Long?,
    val controlPointsString: String
) {
    /** Returns the race type that should be used for this category. */
    fun effectiveRaceType(race: EventRace): RaceType =
        if (differentProperties) raceType ?: race.raceType else race.raceType

    /** Returns the frequency band that should be used for this category. */
    fun effectiveRaceBand(race: EventRace): RaceBand =
        if (differentProperties) raceBand ?: race.raceBand else race.raceBand

    /** Returns the time limit that should be used for this category, expressed in seconds. */
    fun effectiveTimeLimitSeconds(race: EventRace): Long =
        if (differentProperties) timeLimitSeconds ?: race.timeLimitSeconds else race.timeLimitSeconds
}

/** Portable control-point definition for a category course. */
data class EventControlPoint(
    val id: String,
    val categoryId: String,
    val siCode: Int,
    val type: ControlPointType,
    val order: Int
)

/** Portable display alias for a SportIdent control code. */
data class EventAlias(
    val id: String,
    val raceId: String,
    val siCode: Int,
    val name: String
)

/** Portable competitor record independent of Android Room persistence. */
data class EventCompetitor(
    val id: String,
    val raceId: String,
    val categoryId: String?,
    val firstName: String,
    val lastName: String,
    val club: String,
    val index: String,
    val isMan: Boolean,
    val birthYear: Int?,
    val siNumber: Int?,
    val siRent: Boolean,
    val startNumber: Int,
    val drawnStartTimeSeconds: Long?
) {
    /** Formats the competitor name in the app's existing LASTNAME Firstname style. */
    fun fullName(): String = "${lastName.uppercase()} $firstName"

    /** Formats the competitor name with the assigned start number appended. */
    fun nameWithStartNumber(): String = "${fullName()} ($startNumber)"
}

/** Portable raw punch record, with SportIdent times represented as absolute seconds. */
data class EventPunch(
    val id: String,
    val raceId: String,
    val resultId: String?,
    val cardNumber: Int?,
    val siCode: Int,
    val siTimeSeconds: Long,
    val originalSiTimeSeconds: Long,
    val punchType: SIRecordType,
    val order: Int,
    val punchStatus: PunchStatus,
    val splitSeconds: Long
)

/** Portable result/readout summary for a competitor or unmatched SI card. */
data class EventResult(
    val id: String,
    val raceId: String,
    val competitorId: String?,
    val siNumber: Int?,
    val cardType: Byte,
    val checkTimeSeconds: Long?,
    val startTimeSeconds: Long?,
    val finishTimeSeconds: Long?,
    val readoutDateTimeIso: String,
    val automaticStatus: Boolean,
    val resultStatus: ResultStatus,
    val points: Int,
    val runTimeSeconds: Long,
    val modified: Boolean,
    val sent: Boolean,
    val place: Int = 0
)

/** Portable punch plus optional alias resolved for display. */
data class EventAliasPunch(
    val punch: EventPunch,
    val alias: EventAlias?
)

/** Portable readout data: result summary plus all recorded punches. */
data class EventReadoutData(
    val result: EventResult,
    val punches: List<EventAliasPunch>
)

/** Portable category aggregate containing course and category competitors. */
data class EventCategoryData(
    val category: EventCategory,
    val controlPoints: List<EventControlPoint>,
    val competitors: List<EventCompetitor>
)

/** Portable competitor plus optional category aggregate used by result lists. */
data class EventCompetitorCategory(
    val competitor: EventCompetitor,
    val category: EventCategory?
)

/** Portable competitor aggregate with optional readout data. */
data class EventCompetitorData(
    val competitorCategory: EventCompetitorCategory,
    val readoutData: EventReadoutData?
)

/** Portable complete event aggregate used for project import/export and desktop workflows. */
data class EventRaceData(
    val race: EventRace,
    val categories: List<EventCategoryData>,
    val aliases: List<EventAlias>,
    val competitorData: List<EventCompetitorData>,
    val unmatchedReadoutData: List<EventReadoutData>
)
