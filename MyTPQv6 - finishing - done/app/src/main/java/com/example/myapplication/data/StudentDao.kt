package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY name ASC")
    suspend fun getAllStudentsList(): List<Student>

    @Query("SELECT * FROM students WHERE studentCode = :studentCode")
    suspend fun getStudentByCode(studentCode: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students WHERE studentCode IS NULL OR studentCode = ''")
    suspend fun getStudentsWithoutCode(): List<Student>

    @Query("UPDATE students SET studentCode = :newCode WHERE studentCode = :oldCode")
    suspend fun updateStudentCode(oldCode: String, newCode: String)
} 