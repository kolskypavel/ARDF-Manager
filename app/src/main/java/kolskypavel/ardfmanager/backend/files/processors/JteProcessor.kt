package kolskypavel.ardfmanager.backend.files.processors

import android.content.Context
import kolskypavel.ardfmanager.backend.room.entitity.Race
import kolskypavel.ardfmanager.backend.room.entitity.embeddeds.ReadoutData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream

object JteProcessor {

    @Throws(IOException::class)
    suspend fun exportTextResults(
        context: Context,
        outStream: OutputStream,
        race: Race,
        results: List<ReadoutData>
    ) {
        withContext(Dispatchers.IO) {

        }
    }
}