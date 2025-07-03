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
        id: Date.now().toString(),
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
      const students = await this.getAllStudents();
      const filteredStudents = students.filter(student => student.id !== id);
      
      if (filteredStudents.length === students.length) {
        throw new Error('Student not found');
      }
      
      await AsyncStorage.setItem(STUDENTS_KEY, JSON.stringify(filteredStudents));
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
}

export const studentService = new StudentService();