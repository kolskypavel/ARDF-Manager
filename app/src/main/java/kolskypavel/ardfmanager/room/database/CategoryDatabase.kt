package kolskypavel.ardfmanager.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kolskypavel.ardfmanager.room.dao.CategoryDao
import kolskypavel.ardfmanager.room.entitity.Category

@Database(entities = [Category::class], version = 1, exportSchema = false)
abstract class CategoryDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
}