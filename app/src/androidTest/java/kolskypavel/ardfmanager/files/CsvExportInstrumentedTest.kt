package kolskypavel.ardfmanager.files

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.processors.CsvProcessor
import kolskypavel.ardfmanager.backend.room.ARDFRepository
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.ControlPoint
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.CategoryData
import kolskypavel.ardfmanager.backend.room.enums.ControlPointType
import kolskypavel.ardfmanager.backend.room.enums.RaceType
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CsvExportInstrumentedTest {

    @Test
    fun testCategoryCSVExport() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        ARDFRepository.initialize(appContext)
        DataProcessor.initialize(appContext)

        val stream = ByteArrayOutputStream()
        val categoryData = ArrayList<CategoryData>()
        val controlPoints = ArrayList<ControlPoint>()

        //Mock the control points
        for (i in 1..5) {
            controlPoints.add(
                ControlPoint(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    30 + i,
                    null,
                    ControlPointType.CONTROL,
                    i,
                    1
                )
            )
        }
        val category = Category(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "M20",
            true,
            40,
            4.3F,
            30F,
            0,
            true,
            RaceType.CLASSICS,
            Duration.ofMinutes(120),
            null,
            null
        )


        categoryData.add(
            CategoryData(
                category,
                controlPoints, emptyList()
            )
        )
        runBlocking {
            CsvProcessor.exportCategories(categoryData, stream)
        }
        val expected = ""
        Assert.assertEquals(expected, stream.toString())
    }
}