package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProgressEntity::class,
        BookmarkEntity::class,
        AppSettingEntity::class,
        CachedAyahEntity::class,
        CachedSurahEntity::class,
        CachedPrayerTimeEntity::class,
        NotificationEntity::class,
        QuranAyahEntity::class
    ],
    version = 24,
    exportSchema = true
)
abstract class CompanionDatabase : RoomDatabase() {
    abstract fun companionDao(): CompanionDao

    companion object {


        private val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_progress ADD COLUMN fajrOnTimeCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE user_progress ADD COLUMN ishaOnTimeCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_19_20 = object : androidx.room.migration.Migration(19, 20) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE cached_ayahs ADD COLUMN translation TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_20_21 = object : androidx.room.migration.Migration(20, 21) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `notifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `time` TEXT NOT NULL, `iconId` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_21_22 = object : androidx.room.migration.Migration(21, 22) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE app_settings ADD COLUMN quranShowTranslation INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_22_23 = object : androidx.room.migration.Migration(22, 23) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `quran_ayahs` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `sura` INTEGER NOT NULL,
                        `ayah` INTEGER NOT NULL,
                        `arabicText` TEXT NOT NULL
                    )"""
                )
            }
        }

        private val MIGRATION_23_24 = object : androidx.room.migration.Migration(23, 24) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Migrate old quran.com numeric reciter IDs to new ar.* format
                database.execSQL("UPDATE app_settings SET quranReciter = 'ar.alafasy'         WHERE quranReciter IN ('7', '1', '2', '3', '4', '5', '6', '8', '9', '10')")
            }
        }

        fun buildDatabase(context: Context): CompanionDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CompanionDatabase::class.java,
                "companion-db"
            )
            .addMigrations(MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24)
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
