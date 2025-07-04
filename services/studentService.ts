import AsyncStorage from '@react-native-async-storage/async-storage';

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

const STUDENTS_KEY = 'tpq_students';

// Generate ID with format YYMMDDHHmmss
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
  async getAllStudents(): Promise<Student[]> {
    try {
      const data = await AsyncStorage.getItem(STUDENTS_KEY);
      return data ? JSON.parse(data) : [];
    } catch (error) {
      console.error('Error getting students:', error);
      return [];
    }
  }

  async getStudentById(id: string): Promise<Student | null> {
    try {
      const students = await this.getAllStudents();
      return students.find(student => student.id === id) || null;
    } catch (error) {
      console.error('Error getting student by id:', error);
      return null;
    }
  }

  async addStudent(studentData: Omit<Student, 'id' | 'createdAt' | 'updatedAt'>): Promise<Student> {
    try {
      const students = await this.getAllStudents();
      const newStudent: Student = {
        ...studentData,
        id: generateStudentId(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      
      students.push(newStudent);
      await AsyncStorage.setItem(STUDENTS_KEY, JSON.stringify(students));
      
      return newStudent;
    } catch (error) {
      console.error('Error adding student:', error);
      throw error;
    }
  }

  async updateStudent(id: string, updates: Partial<Omit<Student, 'id' | 'createdAt'>>): Promise<Student | null> {
    try {
      const students = await this.getAllStudents();
      const index = students.findIndex(student => student.id === id);
      
      if (index === -1) {
        throw new Error('Student not found');
      }
      
      students[index] = {
        ...students[index],
        ...updates,
        updatedAt: new Date().toISOString(),
      };
      
      await AsyncStorage.setItem(STUDENTS_KEY, JSON.stringify(students));
      return students[index];
    } catch (error) {
      console.error('Error updating student:', error);
      throw error;
    }
  }

  async deleteStudent(id: string): Promise<boolean> {
    try {
      console.log('Attempting to delete student with ID:', id);
      const students = await this.getAllStudents();
      console.log('Current students count:', students.length);
      
      const filteredStudents = students.filter(student => student.id !== id);
      console.log('Filtered students count:', filteredStudents.length);
      
      if (filteredStudents.length === students.length) {
        console.log('Student not found for deletion');
        throw new Error('Student not found');
      }
      
      await AsyncStorage.setItem(STUDENTS_KEY, JSON.stringify(filteredStudents));
      console.log('Student deleted successfully');
      return true;
    } catch (error) {
      console.error('Error deleting student:', error);
      throw error;
    }
  }

  async searchStudents(query: string): Promise<Student[]> {
    try {
      const students = await this.getAllStudents();
      const lowercaseQuery = query.toLowerCase();
      
      return students.filter(student =>
        student.name.toLowerCase().includes(lowercaseQuery) ||
        student.readingLevel.toLowerCase().includes(lowercaseQuery)
      );
    } catch (error) {
      console.error('Error searching students:', error);
      return [];
    }
  }

  async getStudentsByReadingLevel(level: string): Promise<Student[]> {
    try {
      const students = await this.getAllStudents();
      return students.filter(student => student.readingLevel === level);
    } catch (error) {
      console.error('Error getting students by reading level:', error);
      return [];
    }
  }

  async clearAllStudents(): Promise<void> {
    try {
      await AsyncStorage.removeItem(STUDENTS_KEY);
    } catch (error) {
      console.error('Error clearing students:', error);
      throw error;
    }
  }

  // Method untuk import data dengan logika overwrite/add berdasarkan ID
  async importStudents(importedStudents: Student[]): Promise<{
    added: number;
    updated: number;
    errors: string[];
  }> {
    try {
      const existingStudents = await this.getAllStudents();
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
          } else {
            // ID baru, tambahkan sebagai siswa baru
            const newStudent = {
              ...importedStudent,
              createdAt: importedStudent.createdAt || new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
            existingStudents.push(newStudent);
            added++;
          }
        } catch (error) {
          errors.push(`Error processing student ${importedStudent.name}: ${error}`);
        }
      }
      
      await AsyncStorage.setItem(STUDENTS_KEY, JSON.stringify(existingStudents));
      
      return { added, updated, errors };
    } catch (error) {
      console.error('Error importing students:', error);
      throw error;
    }
  }
}

export const studentService = new StudentService();