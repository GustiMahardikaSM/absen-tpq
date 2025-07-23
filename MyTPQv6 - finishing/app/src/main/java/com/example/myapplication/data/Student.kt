package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val gender: String? = null,
    val birthDate: Long? = null,
    val positionType: String? = null,
    val iqroNumber: Int? = null,
    val iqroPage: Int? = null,
    val quranSurah: Int? = null,
    val quranAyat: Int? = null,
    val studentCode: String? = null
) 