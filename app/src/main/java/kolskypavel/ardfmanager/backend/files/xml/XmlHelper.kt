package kolskypavel.ardfmanager.backend.files.xml

import android.content.Context
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.helpers.ControlPointsHelper
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Competitor
import kolskypavel.ardfmanager.backend.room.entity.Race
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.AliasPunch
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CompetitorData
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.ReadoutData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.ResultStatus
import kolskypavel.ardfmanager.backend.room.enums.SIRecordType
import kolskypavel.ardfmanager.backend.wrappers.ResultWrapper
import org.openardf.radioomanager.shared.results.IofResultStatus
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import org.xmlpull.v1.XmlPullParser
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/** XML utility functions for IOF 3.0 course, start-list, and result-list exchange. */
object XmlHelper {

    /** Creates a UTF-8 XML serializer and starts the XML document. */
    fun createSerializer(outStream: OutputStream): Pair<XmlSerializer, OutputStreamWriter> {
        val factory = XmlPullParserFactory.newInstance()
        val serializer = factory.newSerializer()
        val writer = OutputStreamWriter(outStream, StandardCharsets.UTF_8)
        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        return Pair(serializer, writer)
    }

    /** Ends and flushes a serializer, ignoring close-time XML/IO errors from best-effort exports. */
    fun finishSerializer(serializer: XmlSerializer, writer: OutputStreamWriter) {
        try {
            serializer.endDocument()
        } catch (_: Exception) {
        }
        try {
            writer.flush()
        } catch (_: Exception) {
        }
    }


    /** Creates a UTF-8 pull parser for IOF XML input. */
    fun createParser(inStream: InputStream): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        val reader = InputStreamReader(inStream, StandardCharsets.UTF_8)
        parser.setInput(reader)
        return parser
    }

    /**
     * Parses IOF course data into category aggregates.
     *
     * The importer reads Course elements inside RaceCourseData, maps course controls to category
     * control points, and uses conservative defaults for optional values that are absent.
     */
    fun parseCategories(inStream: InputStream, race: Race, context: Context): List<CategoryData> {
        val parser = createParser(inStream)
        val categories = ArrayList<CategoryData>()
        var order = 0

        var parserEvent = parser.eventType
        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            if (parserEvent == XmlPullParser.START_TAG && parser.name == "RaceCourseData") {
                // IOF Course elements are treated as Radio-O-Manager categories.
                var innerEvent = parser.next()
                while (!(innerEvent == XmlPullParser.END_TAG && parser.name == "RaceCourseData")) {
                    if (innerEvent == XmlPullParser.START_TAG && parser.name == "Course") {
                        // Each imported course gets fresh local ids for the category and controls.
                        val categoryId = UUID.randomUUID()
                        var courseEvent = parser.next()
                        var name = ""
                        var lengthVal = 0
                        var climbVal = 0
                        val controlPoints = ArrayList<ControlPoint>()

                        while (!(courseEvent == XmlPullParser.END_TAG && parser.name == "Course")) {
                            if (courseEvent == XmlPullParser.START_TAG) {
                                when (parser.name) {
                                    "Name" -> {
                                        name = parser.nextText().trim()
                                    }

                                    "Length" -> {
                                        lengthVal = parser.nextText().trim().toIntOrNull() ?: 0
                                    }

                                    "Climb" -> {
                                        climbVal = parser.nextText().trim().toIntOrNull() ?: 0
                                    }

                                    "CourseControl" -> {
                                        val controlType =
                                            parser.getAttributeValue(null, "type")?.trim()
                                                ?: "Control"

                                        if (controlType != "Control") {
                                            skipCurrentElement(parser)
                                        } else {
                                            var siCode = 0
                                            var evEvent = parser.next()
                                            while (!(evEvent == XmlPullParser.END_TAG && parser.name == "CourseControl")) {
                                                if (evEvent == XmlPullParser.START_TAG) {
                                                    if (parser.name == "Control") {
                                                        siCode =
                                                            parser.nextText().trim().toIntOrNull()
                                                                ?: 0
                                                    } else {
                                                        // Advance over unused nested tags, such as LegLength.
                                                        parser.nextText()
                                                    }
                                                }
                                                evEvent = parser.next()
                                            }
                                            controlPoints.add(
                                                ControlPoint(
                                                    UUID.randomUUID(),
                                                    categoryId,
                                                    siCode,
                                                    ControlPointType.CONTROL,
                                                    controlPoints.size
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            courseEvent = parser.next()
                        }

                        // A category without a name cannot be presented or matched later.
                        if (name.isBlank()) {
                            val line = try {
                                parser.lineNumber
                            } catch (_: Exception) {
                                -1
                            }
                            throw IllegalArgumentException(
                                context.getString(
                                    R.string.data_import_category_name_missing,
                                    line
                                )
                            )
                        }

                        val cat = Category(
                            categoryId,
                            race.id,
                            name,
                            true,
                            null,
                            lengthVal,
                            climbVal,
                            order++,
                            false,
                            null,
                            null,
                            null,
                            ControlPointsHelper.getStringFromControlPoints(controlPoints)
                        )

                        categories.add(CategoryData(cat, controlPoints.toList(), emptyList()))
                    }
                    innerEvent = parser.next()
                }
            }
            parserEvent = parser.next()
        }
        return categories
    }

    /** Skips the current XML element, including any nested child elements. */
    private fun skipCurrentElement(parser: XmlPullParser) {
        var depth = 1
        var ev = parser.next()
        while (depth > 0) {
            when (ev) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
            if (depth > 0) ev = parser.next()
        }
    }

    /** Writes the IOF document root and shared event metadata. */
    fun writeRootTag(
        serializer: XmlSerializer,
        race: Race,
        rootTag: String,
        dataProcessor: DataProcessor
    ) {
        serializer.startTag(null, rootTag)
        serializer.attribute(null, "xmlns", "http://www.orienteering.org/datastandard/3.0")
        serializer.attribute(null, "iofVersion", "3.0")
        serializer.attribute(null, "creator", "Radio-O Manager ${dataProcessor.getAppVersion()}")

        // IOF result lists include a status attribute; start lists do not.
        if (rootTag == "ResultList") {
            serializer.attribute(null, "status", "Complete")
        }
        writeRaceTag(serializer, race)
    }


    /** Writes the IOF Event element with race name and start date/time. */
    fun writeRaceTag(serializer: XmlSerializer, race: Race) {
        serializer.startTag(null, "Event")
        writeTextElement(serializer, "Name", race.name)
        serializer.startTag(null, "StartTime")
        writeTextElement(
            serializer,
            "Date",
            TimeProcessor.formatLocalDate(race.startDateTime.toLocalDate())
        )
        writeTextElement(
            serializer,
            "Time",
            TimeProcessor.formatLocalTime(race.startDateTime.toLocalTime())
        )
        serializer.endTag(null, "StartTime")
        serializer.endTag(null, "Event")
    }

    /** Writes one IOF ClassStart block for a category and its competitor starts. */
    fun writeCategoryStartList(
        serializer: XmlSerializer,
        categoryData: CategoryData,
        startZero: LocalDateTime
    ) {
        serializer.startTag(null, "ClassStart")

        serializer.startTag(null, "Class")
        writeTextElement(serializer, "Name", categoryData.category.name)
        serializer.endTag(null, "Class")

        serializer.startTag(null, "Course")
        writeTextElement(serializer, "Length", categoryData.category.length.toString())
        writeTextElement(serializer, "Climb", categoryData.category.climb.toString())
        serializer.endTag(null, "Course")

        for (comp in categoryData.competitors) {
            writePersonStart(serializer, comp, startZero)
        }
        serializer.endTag(null, "ClassStart")
    }

    /** Writes one IOF PersonStart block for a competitor. */
    fun writePersonStart(
        serializer: XmlSerializer,
        competitor: Competitor,
        startZero: LocalDateTime
    ) {
        serializer.startTag(null, "PersonStart")
        writePersonAndClub(serializer, competitor)

        // IOF StartTime is absolute, while competitors store starts relative to race start.
        serializer.startTag(null, "Start")
        writeTextElement(serializer, "BibNumber", competitor.startNumber.toString())

        val start = startZero + (competitor.drawnRelativeStartTime ?: Duration.ZERO)
        writeTextElement(serializer, "StartTime", TimeProcessor.formatIsoLocalDateTime(start))
        competitor.siNumber?.let { si ->
            writeTextElement(
                serializer,
                "ControlCard",
                si.toString()
            )
        }
        serializer.endTag(null, "Start")
        serializer.endTag(null, "PersonStart")
    }

    /** Writes one IOF ClassResult block for a result category. */
    fun writeCategoryResult(
        serializer: XmlSerializer,
        res: ResultWrapper,
        startZero: LocalDateTime
    ) {

        serializer.startTag(null, "ClassResult")
        serializer.startTag(null, "Class")
        serializer.attribute(null, "sex", if (res.category!!.isMan) "M" else "F")
        writeTextElement(serializer, "Name", res.category.name)
        serializer.endTag(null, "Class")

        for (cd in res.competitorData) {
            writePersonResult(serializer, cd, startZero)
        }
        serializer.endTag(null, "ClassResult")
    }

    /** Writes one IOF PersonResult block for a competitor and optional readout. */
    fun writePersonResult(
        serializer: XmlSerializer,
        competitorData: CompetitorData,
        startZero: LocalDateTime
    ) {
        serializer.startTag(null, "PersonResult")

        writePersonAndClub(serializer, competitorData.competitorCategory.competitor)
        writeResult(serializer, competitorData.readoutData, startZero)

        serializer.endTag(null, "PersonResult")
    }

    /** Writes IOF person identity and optional organisation data. */
    private fun writePersonAndClub(
        serializer: XmlSerializer,
        competitor: Competitor
    ) {
        serializer.startTag(null, "Person")

        if (competitor.index.isNotBlank()) {
            serializer.startTag(null, "Id")
            serializer.attribute(null, "type", "CZE")
            serializer.text(competitor.index)
            serializer.endTag(null, "Id")
        }

        serializer.startTag(null, "Name")
        writeTextElement(serializer, "Family", competitor.lastName)
        writeTextElement(serializer, "Given", competitor.firstName)
        serializer.endTag(null, "Name")

        serializer.endTag(null, "Person")

        if (competitor.club.isNotBlank()) {
            serializer.startTag(null, "Organisation")
            writeTextElement(serializer, "Name", competitor.club)
            serializer.endTag(null, "Organisation")
        }
    }


    /** Writes the IOF Result block, including timing, status, place, and splits when present. */
    private fun writeResult(
        serializer: XmlSerializer,
        readout: ReadoutData?,
        startZero: LocalDateTime
    ) {
        serializer.startTag(null, "Result")
        if (readout != null) {
            val res = readout.result
            try {
                res.startTime?.let {
                    writeTextElement(
                        serializer,
                        "StartTime",
                        TimeProcessor.formatIsoLocalDateTime(it.toLocalDateTime(startZero))
                    )
                }
                res.finishTime?.let {
                    writeTextElement(
                        serializer,
                        "FinishTime",
                        TimeProcessor.formatIsoLocalDateTime(it.toLocalDateTime(startZero))
                    )
                }
            } catch (_: Exception) {
            }
            val seconds = res.runTime.toMillis() / 1000
            writeTextElement(serializer, "Time", seconds.toString())

            if (res.place > 0 && res.resultStatus == ResultStatus.OK) {
                writeTextElement(serializer, "Position", res.place.toString())
            }
            writeTextElement(serializer, "Status", convertResultStatus(res.resultStatus))
        } else {
            writeTextElement(serializer, "Status", "Active")
        }

        writeSplitTimes(serializer, readout?.punches)
        serializer.endTag(null, "Result")
    }

    /** Writes cumulative IOF split times for control punches. */
    private fun writeSplitTimes(serializer: XmlSerializer, punches: List<AliasPunch>?) {
        if (punches == null || punches.isEmpty()) return
        var cumulativeMillis: Long = 0
        for (p in punches.filter { it.punch.punchType == SIRecordType.CONTROL }) {
            serializer.startTag(null, "SplitTime")
            val control = p.punch.siCode.toString()
            writeTextElement(serializer, "ControlCode", control)
            try {
                cumulativeMillis += p.punch.split.toMillis()
                val tSeconds = cumulativeMillis / 1000
                writeTextElement(serializer, "Time", tSeconds.toString())
            } catch (_: Exception) {
            }
            serializer.endTag(null, "SplitTime")
        }
    }

    /** Converts the app result status enum to its IOF status string. */
    fun convertResultStatus(resultStatus: ResultStatus): String {
        return IofResultStatus.fromResultStatus(resultStatus)
    }

    /** Writes a simple text-only XML element. */
    fun writeTextElement(serializer: XmlSerializer, name: String, value: String) {
        serializer.startTag(null, name)
        serializer.text(value)
        serializer.endTag(null, name)
    }
}
