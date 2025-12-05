package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Student - Model data untuk entitas siswa/santri TPQ
 * Menyimpan informasi lengkap siswa termasuk data pribadi dan posisi bacaan
 */
@Entity(
    tableName = "students", // Nama tabel dalam database
    indices = [Index(value = ["studentCode"], unique = true)] // Index unik untuk studentCode
)
data class Student(
    @PrimaryKey
    val studentCode: String, // Kode unik siswa (primary key)
    val name: String, // Nama lengkap siswa
    val createdAt: Long = System.currentTimeMillis(), // Timestamp pembuatan data
    val gender: String? = null, // Jenis kelamin (Laki-laki/Perempuan)
    val birthDate: Long? = null, // Tanggal lahir dalam format timestamp
    val positionType: String? = null, // Posisi bacaan (Iqro/Quran)
    val iqroNumber: Int? = null, // Nomor jilid Iqro (0=Pra-TK, 1-6=Jilid 1-6)
    val iqroPage: Int? = null, // Halaman Iqro yang sedang dipelajari
    val quranSurah: Int? = null, // Nomor surah Al-Quran yang sedang dipelajari
    val quranAyat: Int? = null // Nomor ayat Al-Quran yang sedang dipelajari
) 