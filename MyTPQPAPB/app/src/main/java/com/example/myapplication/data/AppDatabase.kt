package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * AppDatabase - Database utama aplikasi TPQ
 * Menggunakan Room Database untuk menyimpan data siswa dan kehadiran
 * Database SQLite dengan nama "tpq_database" versi 6
 */
@Database(
    entities = [Student::class, Attendance::class], // Entitas yang disimpan dalam database
    version = 6, // Versi database saat ini
    exportSchema = false // Tidak export schema untuk production
)
abstract class AppDatabase : RoomDatabase() {
    // DAO untuk operasi data siswa
    abstract fun studentDao(): StudentDao
    // DAO untuk operasi data kehadiran
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton pattern untuk mendapatkan instance database
         * Memastikan hanya ada satu instance database dalam aplikasi
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tpq_database" // Nama database SQLite
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * MIGRATION_1_2 - Migrasi dari versi 1 ke 2
 * Menambahkan kolom baru untuk data lengkap siswa
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tambahkan kolom baru ke tabel students untuk data lengkap
        database.execSQL("ALTER TABLE students ADD COLUMN gender TEXT") // Jenis kelamin
        database.execSQL("ALTER TABLE students ADD COLUMN birthDate INTEGER") // Tanggal lahir
        database.execSQL("ALTER TABLE students ADD COLUMN positionType TEXT") // Posisi bacaan (Iqro/Quran)
        database.execSQL("ALTER TABLE students ADD COLUMN iqroNumber INTEGER") // Nomor jilid Iqro
        database.execSQL("ALTER TABLE students ADD COLUMN iqroPage INTEGER") // Halaman Iqro
        database.execSQL("ALTER TABLE students ADD COLUMN quranSurah INTEGER") // Nomor surah Al-Quran
        database.execSQL("ALTER TABLE students ADD COLUMN quranAyat INTEGER") // Nomor ayat Al-Quran
    }
}

/**
 * MIGRATION_2_3 - Migrasi dari versi 2 ke 3
 * Menambahkan kolom detail bacaan ke tabel attendance
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tambahkan kolom detail bacaan ke tabel attendance
        database.execSQL("ALTER TABLE attendance ADD COLUMN iqroNumber INTEGER") // Jilid Iqro yang dibaca
        database.execSQL("ALTER TABLE attendance ADD COLUMN iqroPage INTEGER") // Halaman Iqro yang dibaca
        database.execSQL("ALTER TABLE attendance ADD COLUMN quranSurah INTEGER") // Surah yang dibaca
        database.execSQL("ALTER TABLE attendance ADD COLUMN quranAyat INTEGER") // Ayat yang dibaca
        database.execSQL("ALTER TABLE attendance ADD COLUMN isPassed INTEGER") // Status lulus/mengulang
    }
}

/**
 * MIGRATION_3_4 - Migrasi dari versi 3 ke 4
 * Menambahkan kolom catatan guru
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tambahkan kolom catatan guru untuk feedback pembelajaran
        database.execSQL("ALTER TABLE attendance ADD COLUMN catatanGuru TEXT")
    }
}

/**
 * MIGRATION_4_5 - Migrasi dari versi 4 ke 5
 * Menambahkan kolom kode siswa
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tambahkan kolom kode siswa untuk identifikasi unik
        database.execSQL("ALTER TABLE students ADD COLUMN studentCode TEXT")
    }
}

/**
 * MIGRATION_5_6 - Migrasi besar dari versi 5 ke 6
 * Mengubah struktur database untuk menggunakan studentCode sebagai primary key
 * dan mengoptimalkan relasi antara tabel students dan attendance
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Backup existing data untuk keamanan
        database.execSQL("CREATE TABLE students_backup AS SELECT * FROM students")
        database.execSQL("CREATE TABLE attendance_backup AS SELECT * FROM attendance")
        
        // Drop existing tables untuk restrukturisasi
        database.execSQL("DROP TABLE attendance")
        database.execSQL("DROP TABLE students")
        
        // Create new students table dengan studentCode sebagai primary key
        database.execSQL("""
            CREATE TABLE students (
                studentCode TEXT PRIMARY KEY NOT NULL,  -- Primary key menggunakan kode siswa
                name TEXT NOT NULL,                     -- Nama siswa
                createdAt INTEGER NOT NULL,             -- Timestamp pembuatan
                gender TEXT,                            -- Jenis kelamin
                birthDate INTEGER,                      -- Tanggal lahir
                positionType TEXT,                      -- Posisi bacaan (Iqro/Quran)
                iqroNumber INTEGER,                     -- Nomor jilid Iqro
                iqroPage INTEGER,                      -- Halaman Iqro
                quranSurah INTEGER,                     -- Nomor surah Al-Quran
                quranAyat INTEGER                       -- Nomor ayat Al-Quran
            )
        """)
        
        // Create new attendance table dengan foreign key ke students
        database.execSQL("""
            CREATE TABLE attendance (
                studentCode TEXT NOT NULL,              -- Foreign key ke students
                date INTEGER NOT NULL,                  -- Tanggal kehadiran
                isPresent INTEGER NOT NULL,             -- Status hadir (1/0)
                createdAt INTEGER NOT NULL,             -- Timestamp pembuatan
                iqroNumber INTEGER,                     -- Jilid Iqro yang dibaca
                iqroPage INTEGER,                       -- Halaman Iqro yang dibaca
                quranSurah INTEGER,                     -- Surah yang dibaca
                quranAyat INTEGER,                      -- Ayat yang dibaca
                isPassed INTEGER,                       -- Status lulus (1/0)
                catatanGuru TEXT,                       -- Catatan dari guru
                PRIMARY KEY(studentCode, date),        -- Composite primary key
                FOREIGN KEY(studentCode) REFERENCES students(studentCode) ON DELETE CASCADE
            )
        """)
        
        // Create index untuk optimasi query attendance berdasarkan studentCode
        database.execSQL("CREATE INDEX index_attendance_studentCode ON attendance(studentCode)")
        
        // Migrate data dari backup dengan mapping studentCode
        database.execSQL("""
            INSERT INTO students (studentCode, name, createdAt, gender, birthDate, positionType, iqroNumber, iqroPage, quranSurah, quranAyat)
            SELECT 
                CASE 
                    WHEN studentCode IS NULL OR studentCode = '' THEN 'STU' || id  -- Generate kode jika kosong
                    ELSE studentCode 
                END as studentCode,
                name, createdAt, gender, birthDate, positionType, iqroNumber, iqroPage, quranSurah, quranAyat
            FROM students_backup
        """)
        
        // Migrate attendance data dengan mapping studentCode yang benar
        database.execSQL("""
            INSERT INTO attendance (studentCode, date, isPresent, createdAt, iqroNumber, iqroPage, quranSurah, quranAyat, isPassed, catatanGuru)
            SELECT 
                CASE 
                    WHEN s.studentCode IS NULL OR s.studentCode = '' THEN 'STU' || a.studentId  -- Generate kode jika kosong
                    ELSE s.studentCode 
                END as studentCode,
                a.date, a.isPresent, a.createdAt, a.iqroNumber, a.iqroPage, a.quranSurah, a.quranAyat, a.isPassed, a.catatanGuru
            FROM attendance_backup a
            JOIN students_backup s ON a.studentId = s.id  -- Join untuk mendapatkan studentCode
        """)
        
        // Cleanup: Drop backup tables setelah migrasi berhasil
        database.execSQL("DROP TABLE students_backup")
        database.execSQL("DROP TABLE attendance_backup")
    }
} 