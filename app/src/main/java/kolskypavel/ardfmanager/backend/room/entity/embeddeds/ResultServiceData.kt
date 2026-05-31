package kolskypavel.ardfmanager.backend.room.entity.embeddeds

import androidx.room.Embedded
import kolskypavel.ardfmanager.backend.room.entity.ResultService

/** Result-service configuration plus count of result rows available for sending. */
data class ResultServiceData(
    @Embedded val resultService: ResultService?,
    val resultCount: Int
)
