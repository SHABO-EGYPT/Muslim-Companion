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
        NotificationEntity::class
    ],
    version = 21,
    exportSchema = false
)
abstract class CompanionDatabase : RoomDatabase() {
    abstract fun companionDao(): CompanionDao

    companion object {
        @Volatile
        private var INSTANCE: CompanionDatabase? = null

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

        fun getDatabase(context: Context): CompanionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompanionDatabase::class.java,
                    "companion-db"
                )
                .addMigrations(MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
