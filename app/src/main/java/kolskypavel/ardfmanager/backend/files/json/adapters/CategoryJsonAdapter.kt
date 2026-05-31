package kolskypavel.ardfmanager.backend.files.json.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kolskypavel.ardfmanager.backend.files.json.temps.CategoryJson
import kolskypavel.ardfmanager.backend.files.json.temps.ControlPointJson
import kolskypavel.ardfmanager.backend.helpers.ControlPointsHelper
import kolskypavel.ardfmanager.backend.helpers.TimeProcessor
import kolskypavel.ardfmanager.backend.room.entity.Category
import kolskypavel.ardfmanager.backend.room.entity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import java.util.UUID

/** Moshi adapter for converting category Room aggregates to the race JSON schema. */
class CategoryJsonAdapter(val raceId: UUID) {
    /** Serializes a category, including its ordered control points. */
    @ToJson
    fun toJson(categoryData: CategoryData): CategoryJson {
        val category = categoryData.category
        return CategoryJson(
            category_name = category.name,
            category_gender = category.isMan,
            category_max_age = category.maxAge,
            category_length = category.length,
            category_climb = category.climb,
            category_different_properties = category.differentProperties,
            category_race_type = category.raceType,
            category_time_limit = category.timeLimit?.let {
                TimeProcessor.durationToFormattedString(
                    it,
                    true
                )
            }
                ?: "",
            category_band = category.categoryBand,
            category_control_points = categoryData.controlPoints.map { cp ->
                ControlPointJson(cp.siCode, cp.type)
            }
        )
    }

    /** Deserializes a category and recreates its control points with fresh local identifiers. */
    @FromJson
    fun fromJson(categoryJson: CategoryJson): CategoryData {
        val catId = UUID.randomUUID()

        val controlPoints = categoryJson.category_control_points.mapIndexed { index, json ->
            ControlPoint(
                UUID.randomUUID(),
                catId,
                json.si_code,
                ControlPointType.CONTROL,
                index
            )
        }

        val category = Category(
            id = catId,
            raceId = raceId,
            name = categoryJson.category_name,
            isMan = categoryJson.category_gender,
            maxAge = categoryJson.category_max_age,
            length = categoryJson.category_length ?: 0,
            climb = categoryJson.category_climb ?: 0,
            order = 0,
            differentProperties = categoryJson.category_different_properties,
            raceType = categoryJson.category_race_type,
            categoryBand = categoryJson.category_band,
            timeLimit = if (categoryJson.category_time_limit?.isNotBlank() == true) {
                TimeProcessor.minuteStringToDuration(categoryJson.category_time_limit)
            } else null,
            controlPointsString = ControlPointsHelper.getStringFromControlPoints(controlPoints)
        )

        return CategoryData(category, controlPoints, emptyList())
    }
}
