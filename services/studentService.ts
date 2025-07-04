import { storageService } from './storageService';

export interface Student {
  id: string;
  name: string;
  birthDate: string;
  gender: 'Laki-laki' | 'Perempuan';
  readingLevel: string;
  currentPosition: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

const STUDENTS_KEY = 'tpq_students_v2';

// Generate ID dengan format YYMMDDHHmmss
const generateStudentId = (): string => {
  const now = new Date();
  const year = now.getFullYear().toString().slice(-2);
  const month = (now.getMonth() + 1).toString().padStart(2, '0');
  const day = now.getDate().toString().padStart(2, '0');
  const hour = now.getHours().toString().padStart(2, '0');
  const minute = now.getMinutes().toString().padStart(2, '0');
  const second = now.getSeconds().toString().padStart(2, '0');
  
  return `${year}${month}${day}${hour}${minute}${second}`;
};

class StudentService {
  // CREATE - Tambah siswa baru
  async addStudent(studentData: Omit<Student, 'id' | 'createdAt' | 'updatedAt'>): Promise<Student> {
    try {
      console.log('Adding new student:', studentData.name);
      
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      const newStudent: Student = {
        ...studentData,
        id: generateStudentId(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      
      students.push(newStudent);
      const saved = await storageService.saveData(STUDENTS_KEY, students);
      
      if (!saved) {
        throw new Error('Failed to save student data');
      }
      
      console.log('Student added successfully:', newStudent.id);
      return newStudent;
    } catch (error) {
      console.error('Error adding student:', error);
      throw error;
    }
  }

  // READ - Ambil semua siswa
  async getAllStudents(): Promise<Student[]> {
    try {
      console.log('Getting all students...');
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      console.log(`Retrieved ${students.length} students`);
      return students;
    } catch (error) {
      console.error('Error getting all students:', error);
      return [];
    }
  }

  // READ - Ambil siswa berdasarkan ID
  async getStudentById(id: string): Promise<Student | null> {
    try {
      console.log('Getting student by ID:', id);
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      const student = students.find(s => s.id === id);
      console.log('Student found:', !!student);
      return student || null;
    } catch (error) {
      console.error('Error getting student by ID:', error);
      return null;
    }
  }

  // UPDATE - Update data siswa
  async updateStudent(id: string, updates: Partial<Omit<Student, 'id' | 'createdAt'>>): Promise<Student | null> {
    try {
      console.log('Updating student:', id);
      
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      const index = students.findIndex(s => s.id === id);
      
      if (index === -1) {
        console.log('Student not found for update:', id);
        throw new Error('Student not found');
      }
      
      students[index] = {
        ...students[index],
        ...updates,
        updatedAt: new Date().toISOString(),
      };
      
      const saved = await storageService.saveData(STUDENTS_KEY, students);
      
      if (!saved) {
        throw new Error('Failed to save updated student data');
      }
      
      console.log('Student updated successfully:', id);
      return students[index];
    } catch (error) {
      console.error('Error updating student:', error);
      throw error;
    }
  }

  // DELETE - Hapus siswa
  async deleteStudent(id: string): Promise<boolean> {
    try {
      console.log('Deleting student:', id);
      
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      const initialCount = students.length;
      
      const filteredStudents = students.filter(s => s.id !== id);
      
      if (filteredStudents.length === initialCount) {
        console.log('Student not found for deletion:', id);
        throw new Error('Student not found');
      }
      
      const saved = await storageService.saveData(STUDENTS_KEY, filteredStudents);
      
      if (!saved) {
        throw new Error('Failed to save after deletion');
      }
      
      console.log('Student deleted successfully:', id);
      return true;
    } catch (error) {
      console.error('Error deleting student:', error);
      throw error;
    }
  }

  // SEARCH - Cari siswa
  async searchStudents(query: string): Promise<Student[]> {
    try {
      console.log('Searching students with query:', query);
      
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      const lowercaseQuery = query.toLowerCase();
      
      const results = students.filter(student =>
        student.name.toLowerCase().includes(lowercaseQuery) ||
        student.readingLevel.toLowerCase().includes(lowercaseQuery)
      );
      
      console.log(`Found ${results.length} students matching query`);
      return results;
    } catch (error) {
      console.error('Error searching students:', error);
      return [];
    }
  }

  // FILTER - Filter siswa berdasarkan level bacaan
  async getStudentsByReadingLevel(level: string): Promise<Student[]> {
    try {
      console.log('Getting students by reading level:', level);
      
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      const results = students.filter(student => student.readingLevel === level);
      
      console.log(`Found ${results.length} students with reading level ${level}`);
      return results;
    } catch (error) {
      console.error('Error getting students by reading level:', error);
      return [];
    }
  }

  // IMPORT - Import data siswa dengan logika overwrite/add
  async importStudents(importedStudents: Student[]): Promise<{
    added: number;
    updated: number;
    errors: string[];
  }> {
    try {
      console.log('Importing students:', importedStudents.length);
      
      const existingStudents = await storageService.getData<Student>(STUDENTS_KEY);
      let added = 0;
      let updated = 0;
      const errors: string[] = [];
      
      for (const importedStudent of importedStudents) {
        try {
          const existingIndex = existingStudents.findIndex(s => s.id === importedStudent.id);
          
          if (existingIndex !== -1) {
            // ID sudah ada, overwrite data
            existingStudents[existingIndex] = {
              ...importedStudent,
              updatedAt: new Date().toISOString(),
            };
            updated++;
            console.log('Updated student:', importedStudent.id);
          } else {
            // ID baru, tambahkan sebagai siswa baru
            const newStudent = {
              ...importedStudent,
              createdAt: importedStudent.createdAt || new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
            existingStudents.push(newStudent);
            added++;
            console.log('Added new student:', importedStudent.id);
          }
        } catch (error) {
          const errorMsg = `Error processing student ${importedStudent.name}: ${error}`;
          console.error(errorMsg);
          errors.push(errorMsg);
        }
      }
      
      const saved = await storageService.saveData(STUDENTS_KEY, existingStudents);
      
      if (!saved) {
        throw new Error('Failed to save imported data');
      }
      
      console.log(`Import completed: ${added} added, ${updated} updated, ${errors.length} errors`);
      return { added, updated, errors };
    } catch (error) {
      console.error('Error importing students:', error);
      throw error;
    }
  }

  // CLEAR - Hapus semua data siswa
  async clearAllStudents(): Promise<boolean> {
    try {
      console.log('Clearing all students...');
      const cleared = await storageService.clearData(STUDENTS_KEY);
      console.log('All students cleared:', cleared);
      return cleared;
    } catch (error) {
      console.error('Error clearing students:', error);
      return false;
    }
  }

  // BACKUP - Backup data siswa
  async backupStudents(): Promise<Student[]> {
    try {
      console.log('Creating backup of students...');
      const students = await storageService.getData<Student>(STUDENTS_KEY);
      console.log(`Backed up ${students.length} students`);
      return students;
    } catch (error) {
      console.error('Error backing up students:', error);
      return [];
    }
  }
}

export const studentService = new StudentService();