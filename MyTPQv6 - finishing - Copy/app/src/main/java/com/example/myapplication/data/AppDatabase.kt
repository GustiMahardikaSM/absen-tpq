package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Student::class, Attendance::class],
    version = 6,
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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
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

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Backup existing data
        database.execSQL("CREATE TABLE students_backup AS SELECT * FROM students")
        database.execSQL("CREATE TABLE attendance_backup AS SELECT * FROM attendance")
        
        // Drop existing tables
        database.execSQL("DROP TABLE attendance")
        database.execSQL("DROP TABLE students")
        
        // Create new students table with studentCode as primary key
        database.execSQL("""
            CREATE TABLE students (
                studentCode TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                gender TEXT,
                birthDate INTEGER,
                positionType TEXT,
                iqroNumber INTEGER,
                iqroPage INTEGER,
                quranSurah INTEGER,
                quranAyat INTEGER
            )
        """)
        
        // Create new attendance table with studentCode as foreign key
        database.execSQL("""
            CREATE TABLE attendance (
                studentCode TEXT NOT NULL,
                date INTEGER NOT NULL,
                isPresent INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                iqroNumber INTEGER,
                iqroPage INTEGER,
                quranSurah INTEGER,
                quranAyat INTEGER,
                isPassed INTEGER,
                catatanGuru TEXT,
                PRIMARY KEY(studentCode, date),
                FOREIGN KEY(studentCode) REFERENCES students(studentCode) ON DELETE CASCADE
            )
        """)
        
        // Create index for attendance
        database.execSQL("CREATE INDEX index_attendance_studentCode ON attendance(studentCode)")
        
        // Migrate data from backup
        database.execSQL("""
            INSERT INTO students (studentCode, name, createdAt, gender, birthDate, positionType, iqroNumber, iqroPage, quranSurah, quranAyat)
            SELECT 
                CASE 
                    WHEN studentCode IS NULL OR studentCode = '' THEN 'STU' || id 
                    ELSE studentCode 
                END as studentCode,
                name, createdAt, gender, birthDate, positionType, iqroNumber, iqroPage, quranSurah, quranAyat
            FROM students_backup
        """)
        
        database.execSQL("""
            INSERT INTO attendance (studentCode, date, isPresent, createdAt, iqroNumber, iqroPage, quranSurah, quranAyat, isPassed, catatanGuru)
            SELECT 
                CASE 
                    WHEN s.studentCode IS NULL OR s.studentCode = '' THEN 'STU' || a.studentId 
                    ELSE s.studentCode 
                END as studentCode,
                a.date, a.isPresent, a.createdAt, a.iqroNumber, a.iqroPage, a.quranSurah, a.quranAyat, a.isPassed, a.catatanGuru
            FROM attendance_backup a
            JOIN students_backup s ON a.studentId = s.id
        """)
        
        // Drop backup tables
        database.execSQL("DROP TABLE students_backup")
        database.execSQL("DROP TABLE attendance_backup")
    }
} 