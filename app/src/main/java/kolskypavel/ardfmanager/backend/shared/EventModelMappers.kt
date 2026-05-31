package kolskypavel.ardfmanager.backend.shared

import kolskypavel.ardfmanager.backend.room.entity.Alias
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.Punch
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.Result
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorCategory
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.RaceData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ReadoutData
import org.openardf.radioomanager.shared.event.EventAlias
import org.openardf.radioomanager.shared.event.EventAliasPunch
import org.openardf.radioomanager.shared.event.EventCategory
import org.openardf.radioomanager.shared.event.EventCategoryData
import org.openardf.radioomanager.shared.event.EventCompetitor
import org.openardf.radioomanager.shared.event.EventCompetitorCategory
import org.openardf.radioomanager.shared.event.EventCompetitorData
import org.openardf.radioomanager.shared.event.EventControlPoint
import org.openardf.radioomanager.shared.event.EventPunch
import org.openardf.radioomanager.shared.event.EventRace
import org.openardf.radioomanager.shared.event.EventRaceData
import org.openardf.radioomanager.shared.event.EventReadoutData
import org.openardf.radioomanager.shared.event.EventResult
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/** Converts the Android Room race entity into the portable shared event model. */
fun Race.toEventRace(): EventRace =
    EventRace(
        id = id.toString(),
        name = name,
        apiKey = apiKey,
        startDateTimeIso = startDateTime.toString(),
        raceType = raceType,
        raceLevel = raceLevel,
        raceBand = raceBand,
        timeLimitSeconds = timeLimit.seconds
    )

/** Converts the Android Room category entity into the portable shared event model. */
fun Category.toEventCategory(): EventCategory =
    EventCategory(
        id = id.toString(),
        raceId = raceId.toString(),
        name = name,
        isMan = isMan,
        maxAge = maxAge,
        lengthMeters = length,
        climbMeters = climb,
        order = order,
        differentProperties = differentProperties,
        raceType = raceType,
        raceBand = categoryBand,
        timeLimitSeconds = timeLimit?.seconds,
        controlPointsString = controlPointsString
    )

/** Converts the Android Room control-point entity into the portable shared event model. */
fun ControlPoint.toEventControlPoint(): EventControlPoint =
    EventControlPoint(
        id = id.toString(),
        categoryId = categoryId.toString(),
        siCode = siCode,
        type = type,
        order = order
    )

/** Converts the Android Room alias entity into the portable shared event model. */
fun Alias.toEventAlias(): EventAlias =
    EventAlias(
        id = id.toString(),
        raceId = raceId.toString(),
        siCode = siCode,
        name = name
    )

/** Converts the Android Room competitor entity into the portable shared event model. */
fun Competitor.toEventCompetitor(): EventCompetitor =
    EventCompetitor(
        id = id.toString(),
        raceId = raceId.toString(),
        categoryId = categoryId?.toString(),
        firstName = firstName,
        lastName = lastName,
        club = club,
        index = index,
        isMan = isMan,
        birthYear = birthYear,
        siNumber = siNumber,
        siRent = siRent,
        startNumber = startNumber,
        drawnStartTimeSeconds = drawnRelativeStartTime?.seconds
    )

/** Converts the Android Room punch entity into the portable shared event model. */
fun Punch.toEventPunch(): EventPunch =
    EventPunch(
        id = id.toString(),
        raceId = raceId.toString(),
        resultId = resultId?.toString(),
        cardNumber = cardNumber,
        siCode = siCode,
        siTimeSeconds = siTime.getSeconds(),
        originalSiTimeSeconds = origSiTime.getSeconds(),
        punchType = punchType,
        order = order,
        punchStatus = punchStatus,
        splitSeconds = split.seconds
    )

/** Converts the Android Room result entity into the portable shared event model. */
fun Result.toEventResult(): EventResult =
    EventResult(
        id = id.toString(),
        raceId = raceId.toString(),
        competitorId = competitorId?.toString(),
        siNumber = siNumber,
        cardType = cardType,
        checkTimeSeconds = checkTime?.getSeconds(),
        startTimeSeconds = startTime?.getSeconds(),
        finishTimeSeconds = finishTime?.getSeconds(),
        readoutDateTimeIso = readoutTime.toString(),
        automaticStatus = automaticStatus,
        resultStatus = resultStatus,
        points = points,
        runTimeSeconds = runTime.seconds,
        modified = modified,
        sent = sent,
        place = place
    )

/** Converts an Android alias-punch relation into the portable shared event model. */
fun AliasPunch.toEventAliasPunch(): EventAliasPunch =
    EventAliasPunch(
        punch = punch.toEventPunch(),
        alias = alias?.toEventAlias()
    )

/** Converts an Android readout aggregate into the portable shared event model. */
fun ReadoutData.toEventReadoutData(): EventReadoutData =
    EventReadoutData(
        result = result.toEventResult(),
        punches = punches.map { it.toEventAliasPunch() }
    )

/** Converts an Android category aggregate into the portable shared event model. */
fun CategoryData.toEventCategoryData(): EventCategoryData =
    EventCategoryData(
        category = category.toEventCategory(),
        controlPoints = controlPoints.map { it.toEventControlPoint() },
        competitors = competitors.map { it.toEventCompetitor() }
    )

/** Converts an Android competitor/category relation into the portable shared event model. */
fun CompetitorCategory.toEventCompetitorCategory(): EventCompetitorCategory =
    EventCompetitorCategory(
        competitor = competitor.toEventCompetitor(),
        category = category?.toEventCategory()
    )

/** Converts an Android competitor aggregate into the portable shared event model. */
fun CompetitorData.toEventCompetitorData(): EventCompetitorData =
    EventCompetitorData(
        competitorCategory = competitorCategory.toEventCompetitorCategory(),
        readoutData = readoutData?.toEventReadoutData()
    )

/** Converts a complete Android Room race aggregate into the portable shared event model. */
fun RaceData.toEventRaceData(): EventRaceData =
    EventRaceData(
        race = race.toEventRace(),
        categories = categories.map { it.toEventCategoryData() },
        aliases = aliases.map { it.toEventAlias() },
        competitorData = competitorData.map { it.toEventCompetitorData() },
        unmatchedReadoutData = unmatchedReadoutData.map { it.toEventReadoutData() }
    )

/** Converts the portable shared race model back into an Android Room entity. */
fun EventRace.toRoomRace(): Race =
    Race(
        id = UUID.fromString(id),
        name = name,
        apiKey = apiKey,
        startDateTime = LocalDateTime.parse(startDateTimeIso),
        raceType = raceType,
        raceLevel = raceLevel,
        raceBand = raceBand,
        timeLimit = Duration.ofSeconds(timeLimitSeconds)
    )

/** Converts the portable shared category model back into an Android Room entity. */
fun EventCategory.toRoomCategory(): Category =
    Category(
        id = UUID.fromString(id),
        raceId = UUID.fromString(raceId),
        name = name,
        isMan = isMan,
        maxAge = maxAge,
        length = lengthMeters,
        climb = climbMeters,
        order = order,
        differentProperties = differentProperties,
        raceType = raceType,
        categoryBand = raceBand,
        timeLimit = timeLimitSeconds?.let(Duration::ofSeconds),
        controlPointsString = controlPointsString
    )

/** Converts the portable shared control-point model back into an Android Room entity. */
fun EventControlPoint.toRoomControlPoint(): ControlPoint =
    ControlPoint(
        id = UUID.fromString(id),
        categoryId = UUID.fromString(categoryId),
        siCode = siCode,
        type = type,
        order = order
    )

/** Converts the portable shared alias model back into an Android Room entity. */
fun EventAlias.toRoomAlias(): Alias =
    Alias(
        id = UUID.fromString(id),
        raceId = UUID.fromString(raceId),
        siCode = siCode,
        name = name
    )

/** Converts the portable shared competitor model back into an Android Room entity. */
fun EventCompetitor.toRoomCompetitor(): Competitor =
    Competitor(
        id = UUID.fromString(id),
        raceId = UUID.fromString(raceId),
        categoryId = categoryId?.let(UUID::fromString),
        firstName = firstName,
        lastName = lastName,
        club = club,
        index = index,
        isMan = isMan,
        birthYear = birthYear,
        siNumber = siNumber,
        siRent = siRent,
        startNumber = startNumber,
        drawnRelativeStartTime = drawnStartTimeSeconds?.let(Duration::ofSeconds)
    )

/** Converts the portable shared punch model back into an Android Room entity. */
fun EventPunch.toRoomPunch(): Punch =
    Punch(
        id = UUID.fromString(id),
        raceId = UUID.fromString(raceId),
        resultId = resultId?.let(UUID::fromString),
        cardNumber = cardNumber,
        siCode = siCode,
        siTime = kolskypavel.ardfmanager.backend.sportident.SITime(siTimeSeconds),
        origSiTime = kolskypavel.ardfmanager.backend.sportident.SITime(originalSiTimeSeconds),
        punchType = punchType,
        order = order,
        punchStatus = punchStatus,
        split = Duration.ofSeconds(splitSeconds)
    )

/** Converts the portable shared result model back into an Android Room entity. */
fun EventResult.toRoomResult(): Result =
    Result(
        id = UUID.fromString(id),
        raceId = UUID.fromString(raceId),
        competitorId = competitorId?.let(UUID::fromString),
        siNumber = siNumber,
        cardType = cardType,
        checkTime = checkTimeSeconds?.let { kolskypavel.ardfmanager.backend.sportident.SITime(it) },
        startTime = startTimeSeconds?.let { kolskypavel.ardfmanager.backend.sportident.SITime(it) },
        finishTime = finishTimeSeconds?.let { kolskypavel.ardfmanager.backend.sportident.SITime(it) },
        readoutTime = LocalDateTime.parse(readoutDateTimeIso),
        automaticStatus = automaticStatus,
        resultStatus = resultStatus,
        points = points,
        runTime = Duration.ofSeconds(runTimeSeconds),
        modified = modified,
        sent = sent
    ).also { it.place = place }

/** Converts the portable shared alias-punch model back into an Android relation object. */
fun EventAliasPunch.toRoomAliasPunch(): AliasPunch =
    AliasPunch(
        punch = punch.toRoomPunch(),
        alias = alias?.toRoomAlias()
    )

/** Converts the portable shared readout model back into an Android aggregate. */
fun EventReadoutData.toRoomReadoutData(): ReadoutData =
    ReadoutData(
        result = result.toRoomResult(),
        punches = punches.map { it.toRoomAliasPunch() }
    )

/** Converts the portable shared category aggregate back into an Android aggregate. */
fun EventCategoryData.toRoomCategoryData(): CategoryData =
    CategoryData(
        category = category.toRoomCategory(),
        controlPoints = controlPoints.map { it.toRoomControlPoint() },
        competitors = competitors.map { it.toRoomCompetitor() }
    )

/** Converts the portable shared competitor/category model back into an Android relation object. */
fun EventCompetitorCategory.toRoomCompetitorCategory(): CompetitorCategory =
    CompetitorCategory(
        competitor = competitor.toRoomCompetitor(),
        category = category?.toRoomCategory()
    )

/** Converts the portable shared competitor aggregate back into an Android aggregate. */
fun EventCompetitorData.toRoomCompetitorData(): CompetitorData =
    CompetitorData(
        competitorCategory = competitorCategory.toRoomCompetitorCategory(),
        readoutData = readoutData?.toRoomReadoutData()
    )

/** Converts a complete portable shared race aggregate back into Android Room aggregates. */
fun EventRaceData.toRoomRaceData(): RaceData =
    RaceData(
        race = race.toRoomRace(),
        categories = categories.map { it.toRoomCategoryData() },
        aliases = aliases.map { it.toRoomAlias() },
        competitorData = competitorData.map { it.toRoomCompetitorData() },
        unmatchedReadoutData = unmatchedReadoutData.map { it.toRoomReadoutData() }
    )
