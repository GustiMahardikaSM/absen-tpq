package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "students",
    indices = [Index(value = ["studentCode"], unique = true)]
)
data class Student(
    @PrimaryKey
    val studentCode: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val gender: String? = null,
    val birthDate: Long? = null,
    val positionType: String? = null,
    val iqroNumber: Int? = null,
    val iqroPage: Int? = null,
    val quranSurah: Int? = null,
    val quranAyat: Int? = null
) 