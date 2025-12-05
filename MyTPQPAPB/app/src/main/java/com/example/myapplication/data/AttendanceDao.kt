package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * AttendanceDao - Data Access Object untuk operasi database kehadiran
 * Menyediakan fungsi untuk mengelola data kehadiran dan statistik
 */
@Dao
interface AttendanceDao {
    /**
     * Mendapatkan kehadiran siswa pada tanggal tertentu
     * @param studentCode Kode siswa
     * @param date Tanggal dalam format timestamp
     * @return Attendance object atau null jika tidak ada data
     */
    @Query("SELECT * FROM attendance WHERE studentCode = :studentCode AND date = :date")
    suspend fun getAttendanceByStudentAndDate(studentCode: String, date: Long): Attendance?

    /**
     * Mendapatkan kehadiran siswa dalam rentang tanggal
     * @param studentCode Kode siswa
     * @param startDate Tanggal mulai
     * @param endDate Tanggal akhir
     * @return Flow<List<Attendance>> untuk reactive programming
     */
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

    /**
     * Menghitung jumlah kehadiran siswa dalam rentang tanggal
     * @param studentCode Kode siswa
     * @param startDate Tanggal mulai
     * @param endDate Tanggal akhir
     * @return Jumlah hari hadir
     */
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

    /**
     * Menyimpan atau update kehadiran siswa
     * Menggunakan REPLACE strategy untuk menghindari konflik
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: Attendance)

    /**
     * Mendapatkan semua kehadiran pada tanggal tertentu
     * @param date Tanggal dalam format timestamp
     * @return Flow<List<Attendance>> untuk reactive programming
     */
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: Long): Flow<List<Attendance>>

    /**
     * Menghitung jumlah sesi yang lulus dalam rentang tanggal
     * @param studentCode Kode siswa
     * @param startDate Tanggal mulai
     * @param endDate Tanggal akhir
     * @return Jumlah sesi yang lulus
     */
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

    /**
     * Menghitung jumlah sesi yang mengulang dalam rentang tanggal
     * @param studentCode Kode siswa
     * @param startDate Tanggal mulai
     * @param endDate Tanggal akhir
     * @return Jumlah sesi yang mengulang
     */
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

    /**
     * Mendapatkan semua data kehadiran diurutkan berdasarkan tanggal terbaru
     * @return List<Attendance> semua data kehadiran
     */
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    suspend fun getAllAttendances(): List<Attendance>

    /**
     * Mendapatkan kehadiran terakhir siswa
     * @param studentCode Kode siswa
     * @return Attendance object kehadiran terakhir atau null
     */
    @Query("""
        SELECT * FROM attendance 
        WHERE studentCode = :studentCode 
        ORDER BY date DESC 
        LIMIT 1
    """)
    suspend fun getLastAttendanceForStudent(studentCode: String): Attendance?

    /**
     * Mendapatkan kehadiran berdasarkan ID (studentCode + date)
     * @param studentCode Kode siswa
     * @param date Tanggal dalam format timestamp
     * @return Attendance object atau null
     */
    @Query("""
        SELECT * FROM attendance 
        WHERE studentCode = :studentCode 
        AND date = :date
    """)
    suspend fun getAttendanceById(studentCode: String, date: Long): Attendance?

    /**
     * Menyimpan kehadiran baru
     * @param attendance Data kehadiran yang akan disimpan
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)
} 