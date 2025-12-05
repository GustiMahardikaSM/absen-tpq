package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * StudentDao - Data Access Object untuk operasi database siswa
 * Menyediakan fungsi CRUD (Create, Read, Update, Delete) untuk entitas Student
 */
@Dao
interface StudentDao {
    /**
     * Mendapatkan semua siswa dengan Flow untuk reactive programming
     * Data diurutkan berdasarkan nama secara ascending
     */
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    /**
     * Mendapatkan semua siswa dalam bentuk List (untuk operasi non-reactive)
     * Digunakan untuk operasi yang membutuhkan data langsung
     */
    @Query("SELECT * FROM students ORDER BY name ASC")
    suspend fun getAllStudentsList(): List<Student>

    /**
     * Mencari siswa berdasarkan kode siswa
     * @param studentCode Kode unik siswa
     * @return Student object atau null jika tidak ditemukan
     */
    @Query("SELECT * FROM students WHERE studentCode = :studentCode")
    suspend fun getStudentByCode(studentCode: String): Student?

    /**
     * Menyimpan siswa baru atau update jika sudah ada
     * Menggunakan REPLACE strategy untuk menghindari konflik
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    /**
     * Update data siswa yang sudah ada
     */
    @Update
    suspend fun updateStudent(student: Student)

    /**
     * Menghapus siswa dari database
     * Akan menghapus juga semua data attendance terkait (CASCADE)
     */
    @Delete
    suspend fun deleteStudent(student: Student)

    /**
     * Mendapatkan siswa yang belum memiliki kode
     * Digunakan untuk migrasi data dari versi lama
     */
    @Query("SELECT * FROM students WHERE studentCode IS NULL OR studentCode = ''")
    suspend fun getStudentsWithoutCode(): List<Student>

    /**
     * Update kode siswa dari kode lama ke kode baru
     * Digunakan untuk migrasi dan update kode siswa
     */
    @Query("UPDATE students SET studentCode = :newCode WHERE studentCode = :oldCode")
    suspend fun updateStudentCode(oldCode: String, newCode: String)
} 