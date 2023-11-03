package kolskypavel.ardfmanager.room

import android.content.Context
import androidx.room.Room
import kolskypavel.ardfmanager.room.database.CategoryDatabase
import kolskypavel.ardfmanager.room.database.CompetitorDatabase
import kolskypavel.ardfmanager.room.database.ControlPointDatabase
import kolskypavel.ardfmanager.room.database.EventDatabase
import kolskypavel.ardfmanager.room.database.PunchDatabase
import kolskypavel.ardfmanager.room.database.ReadoutDatabase
import kolskypavel.ardfmanager.room.entitity.Event
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ARDFRepository private constructor(context: Context) {

    private val eventDatabase: EventDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            EventDatabase::class.java,
            "event-database"
        )
        .build()

    private val competitorDatabase: CompetitorDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            CompetitorDatabase::class.java,
            "competitor-database"
        )
        .build()

    private val categoryDatabase: CategoryDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            CategoryDatabase::class.java,
            "category-database"
        )
        .build()

    private val controlPointDatabase: ControlPointDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ControlPointDatabase::class.java,
            "control-point-database"
        )
        .build()

    private val readoutDatabase: ReadoutDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ReadoutDatabase::class.java,
            "readout-database"
        )
        .build()

    private val punchDatabase: PunchDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            PunchDatabase::class.java,
            "punch-database"
        )
        .build()

    //Events
    suspend fun getEvents(): Flow<List<Event>> = eventDatabase.eventDao().getEvents()
    suspend fun getEvent(id: UUID): Event = eventDatabase.eventDao().getEvent(id)
    suspend fun createEvent(event: Event) = eventDatabase.eventDao().createEvent(event)
    suspend fun updateEvent(event: Event) = eventDatabase.eventDao().updateEvent(event)
    suspend fun deleteEvent(id: UUID) = eventDatabase.eventDao().deleteEvent(id)


    //Singleton instantiation
    companion object {
        private var INSTANCE: ARDFRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ARDFRepository(context)
            }
        }

        fun get(): ARDFRepository {
            return INSTANCE ?: throw IllegalStateException("ARDFRepository must be initialized")
        }
    }
}