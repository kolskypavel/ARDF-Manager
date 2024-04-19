package kolskypavel.ardfmanager.backend.room.entitity.embeddeds

import androidx.room.Embedded
import androidx.room.Relation
import kolskypavel.ardfmanager.backend.room.entitity.Category
import kolskypavel.ardfmanager.backend.room.entitity.Competitor
import kolskypavel.ardfmanager.backend.room.entitity.Readout
import kolskypavel.ardfmanager.backend.room.entitity.Result

//Main embedded class, providing data
data class CompetitorData(
    @Embedded var competitor: Competitor,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    var category: Category?,
    @Relation(parentColumn = "id", entityColumn = "competitor_id")
    var readout: Readout?,
    @Relation(parentColumn = "id", entityColumn = "competitor_id")
    var result: Result?,
)
