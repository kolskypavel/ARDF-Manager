package org.openardf.radioomanager.shared.event

import org.openardf.radioomanager.shared.domain.ControlPointType
import org.openardf.radioomanager.shared.domain.PunchStatus
import org.openardf.radioomanager.shared.domain.RaceBand
import org.openardf.radioomanager.shared.domain.RaceLevel
import org.openardf.radioomanager.shared.domain.RaceType
import org.openardf.radioomanager.shared.domain.ResultStatus
import org.openardf.radioomanager.shared.domain.SIRecordType

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
    fun effectiveRaceType(race: EventRace): RaceType =
        if (differentProperties) raceType ?: race.raceType else race.raceType

    fun effectiveRaceBand(race: EventRace): RaceBand =
        if (differentProperties) raceBand ?: race.raceBand else race.raceBand

    fun effectiveTimeLimitSeconds(race: EventRace): Long =
        if (differentProperties) timeLimitSeconds ?: race.timeLimitSeconds else race.timeLimitSeconds
}

data class EventControlPoint(
    val id: String,
    val categoryId: String,
    val siCode: Int,
    val type: ControlPointType,
    val order: Int
)

data class EventAlias(
    val id: String,
    val raceId: String,
    val siCode: Int,
    val name: String
)

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
    fun fullName(): String = "${lastName.uppercase()} $firstName"

    fun nameWithStartNumber(): String = "${fullName()} ($startNumber)"
}

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

data class EventAliasPunch(
    val punch: EventPunch,
    val alias: EventAlias?
)

data class EventReadoutData(
    val result: EventResult,
    val punches: List<EventAliasPunch>
)

data class EventCategoryData(
    val category: EventCategory,
    val controlPoints: List<EventControlPoint>,
    val competitors: List<EventCompetitor>
)

data class EventCompetitorCategory(
    val competitor: EventCompetitor,
    val category: EventCategory?
)

data class EventCompetitorData(
    val competitorCategory: EventCompetitorCategory,
    val readoutData: EventReadoutData?
)

data class EventRaceData(
    val race: EventRace,
    val categories: List<EventCategoryData>,
    val aliases: List<EventAlias>,
    val competitorData: List<EventCompetitorData>,
    val unmatchedReadoutData: List<EventReadoutData>
)
