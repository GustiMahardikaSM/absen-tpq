package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Attendance - Model data untuk entitas kehadiran siswa
 * Menyimpan data kehadiran harian dengan detail bacaan dan feedback guru
 */
@Entity(
    tableName = "attendance", // Nama tabel dalam database
    foreignKeys = [
        ForeignKey(
            entity = Student::class, // Relasi ke tabel Student
            parentColumns = ["studentCode"], // Kolom parent (Student)
            childColumns = ["studentCode"], // Kolom child (Attendance)
            onDelete = ForeignKey.CASCADE // Hapus attendance jika student dihapus
        )
    ],
    primaryKeys = ["studentCode", "date"], // Composite primary key
    indices = [Index("studentCode")] // Index untuk optimasi query
)
data class Attendance(
    val studentCode: String, // Kode siswa (foreign key)
    val date: Long, // Tanggal kehadiran dalam format timestamp
    val isPresent: Boolean, // Status kehadiran (true=hadir, false=absen)
    val createdAt: Long = System.currentTimeMillis(), // Timestamp pembuatan data
    val iqroNumber: Int? = null, // Jilid Iqro yang dibaca pada hari itu
    val iqroPage: Int? = null, // Halaman Iqro yang dibaca pada hari itu
    val quranSurah: Int? = null, // Surah Al-Quran yang dibaca pada hari itu
    val quranAyat: Int? = null, // Ayat Al-Quran yang dibaca pada hari itu
    val isPassed: Boolean? = null, // Status lulus/mengulang (true=lulus, false=mengulang)
    val catatanGuru: String? = null // Catatan atau feedback dari guru
) 