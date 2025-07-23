package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentCode"],
            childColumns = ["studentCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["studentCode", "date"],
    indices = [Index("studentCode")]
)
data class Attendance(
    val studentCode: String,
    val date: Long,
    val isPresent: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val iqroNumber: Int? = null,
    val iqroPage: Int? = null,
    val quranSurah: Int? = null,
    val quranAyat: Int? = null,
    val isPassed: Boolean? = null,
    val catatanGuru: String? = null
) 