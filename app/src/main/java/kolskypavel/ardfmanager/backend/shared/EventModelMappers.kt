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

fun ControlPoint.toEventControlPoint(): EventControlPoint =
    EventControlPoint(
        id = id.toString(),
        categoryId = categoryId.toString(),
        siCode = siCode,
        type = type,
        order = order
    )

fun Alias.toEventAlias(): EventAlias =
    EventAlias(
        id = id.toString(),
        raceId = raceId.toString(),
        siCode = siCode,
        name = name
    )

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

fun AliasPunch.toEventAliasPunch(): EventAliasPunch =
    EventAliasPunch(
        punch = punch.toEventPunch(),
        alias = alias?.toEventAlias()
    )

fun ReadoutData.toEventReadoutData(): EventReadoutData =
    EventReadoutData(
        result = result.toEventResult(),
        punches = punches.map { it.toEventAliasPunch() }
    )

fun CategoryData.toEventCategoryData(): EventCategoryData =
    EventCategoryData(
        category = category.toEventCategory(),
        controlPoints = controlPoints.map { it.toEventControlPoint() },
        competitors = competitors.map { it.toEventCompetitor() }
    )

fun CompetitorCategory.toEventCompetitorCategory(): EventCompetitorCategory =
    EventCompetitorCategory(
        competitor = competitor.toEventCompetitor(),
        category = category?.toEventCategory()
    )

fun CompetitorData.toEventCompetitorData(): EventCompetitorData =
    EventCompetitorData(
        competitorCategory = competitorCategory.toEventCompetitorCategory(),
        readoutData = readoutData?.toEventReadoutData()
    )

fun RaceData.toEventRaceData(): EventRaceData =
    EventRaceData(
        race = race.toEventRace(),
        categories = categories.map { it.toEventCategoryData() },
        aliases = aliases.map { it.toEventAlias() },
        competitorData = competitorData.map { it.toEventCompetitorData() },
        unmatchedReadoutData = unmatchedReadoutData.map { it.toEventReadoutData() }
    )
