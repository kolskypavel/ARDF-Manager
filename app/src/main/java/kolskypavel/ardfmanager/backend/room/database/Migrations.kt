package kolskypavel.ardfmanager.backend.room.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration from version 1 -> 2: change category.length and category.climb from REAL/float to INTEGER
// Strategy: create new table category_new with INTEGER columns, copy data casting floats to integers (truncation),
// drop old table, rename new table back to category, recreate index.

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) create new table with desired schema (match EventDatabase_Impl.createAllTables)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `category_new` (
                `id` BLOB NOT NULL,
                `race_id` BLOB NOT NULL,
                `name` TEXT NOT NULL,
                `is_man` INTEGER NOT NULL,
                `max_age` INTEGER,
                `length` INTEGER NOT NULL,
                `climb` INTEGER NOT NULL,
                `order` INTEGER NOT NULL,
                `different_properties` INTEGER NOT NULL,
                `race_type` TEXT,
                `category_band` TEXT,
                `limit` TEXT,
                `control_points_string` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`race_id`) REFERENCES `race`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent()
        )

        // 2) copy data from old table, converting length/climb using CAST (truncates toward zero)
        db.execSQL(
            """
            INSERT INTO category_new (id, race_id, name, is_man, max_age, length, climb, `order`, different_properties, race_type, category_band, `limit`, control_points_string)
            SELECT id, race_id, name, is_man, max_age,
                   CAST(length AS INTEGER),
                   CAST(climb AS INTEGER),
                   `order`, different_properties, race_type, category_band, `limit`, control_points_string
            FROM category
        """.trimIndent()
        )

        // 3) drop old table, rename new to original name
        db.execSQL("DROP TABLE category")
        db.execSQL("ALTER TABLE category_new RENAME TO category")

        // 4) recreate indices expected by Room
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_category_name_race_id` ON `category` (`name`, `race_id`)")

        // 5) add `interval` column to result_service (Duration stored as text). Use default 'PT10S' for existing rows.
        db.execSQL("ALTER TABLE result_service ADD COLUMN `interval` TEXT NOT NULL DEFAULT 'PT10S'")
    }
}

// Migration from version 2 -> 3: drop orig_* columns from `result` table by recreating it without those columns
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create new table matching the Result entity schema (without orig_check_time, orig_start_time, orig_finish_time)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `result_new` (
                `id` BLOB NOT NULL,
                `race_id` BLOB NOT NULL,
                `competitor_id` BLOB,
                `si_number` INTEGER,
                `card_type` INTEGER NOT NULL,
                `check_time` TEXT,
                `start_time` TEXT,
                `finish_time` TEXT,
                `readout_time` TEXT NOT NULL,
                `automatic_status` INTEGER NOT NULL,
                `result_status` TEXT NOT NULL,
                `points` INTEGER NOT NULL,
                `run_time` TEXT NOT NULL,
                `modified` INTEGER NOT NULL,
                `sent` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`race_id`) REFERENCES `race`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`competitor_id`) REFERENCES `competitor`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
        """.trimIndent()
        )

        // Copy data from old table into the new table skipping orig_* columns
        db.execSQL(
            """
            INSERT INTO result_new (id, race_id, competitor_id, si_number, card_type, check_time, start_time, finish_time, readout_time, automatic_status, result_status, points, run_time, modified, sent)
            SELECT id, race_id, competitor_id, si_number, card_type, check_time, start_time, finish_time, readout_time, automatic_status, result_status, points, run_time, modified, sent
            FROM result
        """.trimIndent()
        )

        // Replace old table with the new one
        db.execSQL("DROP TABLE result")
        db.execSQL("ALTER TABLE result_new RENAME TO result")
    }
}
