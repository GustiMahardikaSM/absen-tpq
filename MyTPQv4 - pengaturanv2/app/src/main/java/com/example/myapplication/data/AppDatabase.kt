package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Student::class, Attendance::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tpq_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tambahkan kolom baru ke tabel students
        database.execSQL("ALTER TABLE students ADD COLUMN gender TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN birthDate INTEGER")
        database.execSQL("ALTER TABLE students ADD COLUMN positionType TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN iqroNumber INTEGER")
        database.execSQL("ALTER TABLE students ADD COLUMN iqroPage INTEGER")
        database.execSQL("ALTER TABLE students ADD COLUMN quranSurah INTEGER")
        database.execSQL("ALTER TABLE students ADD COLUMN quranAyat INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE attendance ADD COLUMN iqroNumber INTEGER")
        database.execSQL("ALTER TABLE attendance ADD COLUMN iqroPage INTEGER")
        database.execSQL("ALTER TABLE attendance ADD COLUMN quranSurah INTEGER")
        database.execSQL("ALTER TABLE attendance ADD COLUMN quranAyat INTEGER")
        database.execSQL("ALTER TABLE attendance ADD COLUMN isPassed INTEGER")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE attendance ADD COLUMN catatanGuru TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE students ADD COLUMN studentCode TEXT")
    }
} 