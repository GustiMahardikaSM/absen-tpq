package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun getAttendanceByStudentAndDate(studentId: Long, date: Long): Attendance?

    @Query("""
        SELECT * FROM attendance 
        WHERE studentId = :studentId 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    fun getAttendanceByStudentAndDateRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<Attendance>>

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentId = :studentId 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isPresent = 1
    """)
    suspend fun getAttendanceCountInRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: Long): Flow<List<Attendance>>

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentId = :studentId 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isPassed = 1
    """)
    suspend fun getTotalPassedInRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): Int

    @Query("""
        SELECT COUNT(*) FROM attendance 
        WHERE studentId = :studentId 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isPassed = 0
    """)
    suspend fun getTotalRetakeInRange(
        studentId: Long,
        startDate: Long,
        endDate: Long
    ): Int

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    suspend fun getAllAttendances(): List<Attendance>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun getAttendanceById(studentId: Long, date: Long): Attendance?

    @Insert
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)
} 