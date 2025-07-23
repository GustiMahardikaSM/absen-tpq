package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE studentCode = :studentCode AND date = :date")
    suspend fun getAttendanceByStudentAndDate(studentCode: String, date: Long): Attendance?

    @Query("""
        SELECT * FROM attendance 
        WHERE studentCode = :studentCode 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    fun getAttendanceByStudentAndDateRange(
        studentCode: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Attendance>>

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentCode = :studentCode 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isPresent = 1
    """)
    suspend fun getAttendanceCountInRange(
        studentCode: String,
        startDate: Long,
        endDate: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: Long): Flow<List<Attendance>>

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentCode = :studentCode 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isPassed = 1
    """)
    suspend fun getTotalPassedInRange(
        studentCode: String,
        startDate: Long,
        endDate: Long
    ): Int

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentCode = :studentCode 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isPassed = 0
    """)
    suspend fun getTotalRetakeInRange(
        studentCode: String,
        startDate: Long,
        endDate: Long
    ): Int

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    suspend fun getAllAttendances(): List<Attendance>

    @Query("""
        SELECT * FROM attendance 
        WHERE studentCode = :studentCode 
        ORDER BY date DESC 
        LIMIT 1
    """)
    suspend fun getLastAttendanceForStudent(studentCode: String): Attendance?

    @Query("""
        SELECT * FROM attendance 
        WHERE studentCode = :studentCode 
        AND date = :date
    """)
    suspend fun getAttendanceById(studentCode: String, date: Long): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)
} 