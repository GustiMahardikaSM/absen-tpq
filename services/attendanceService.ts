import AsyncStorage from '@react-native-async-storage/async-storage';

export interface Attendance {
  id: string;
  studentId: string;
  studentName: string;
  date: string; // YYYY-MM-DD format
  present: boolean;
  readingType: 'Iqro' | 'Al-Quran';
  currentReading: string;
  lessonCompleted: boolean;
  teacherNotes: string;
  createdAt: string;
  updatedAt: string;
}

const ATTENDANCE_KEY = 'tpq_attendance';

class AttendanceService {
  async getAllAttendance(): Promise<Attendance[]> {
    try {
      const data = await AsyncStorage.getItem(ATTENDANCE_KEY);
      return data ? JSON.parse(data) : [];
    } catch (error) {
      console.error('Error getting attendance:', error);
      return [];
    }
  }

  async getAttendanceByDate(date: string): Promise<Attendance[]> {
    try {
      const attendance = await this.getAllAttendance();
      return attendance.filter(record => record.date === date);
    } catch (error) {
      console.error('Error getting attendance by date:', error);
      return [];
    }
  }

  async getAttendanceByStudent(studentId: string): Promise<Attendance[]> {
    try {
      const attendance = await this.getAllAttendance();
      return attendance.filter(record => record.studentId === studentId);
    } catch (error) {
      console.error('Error getting attendance by student:', error);
      return [];
    }
  }

  async getAttendanceByDateRange(startDate: string, endDate: string): Promise<Attendance[]> {
    try {
      const attendance = await this.getAllAttendance();
      return attendance.filter(record => 
        record.date >= startDate && record.date <= endDate
      );
    } catch (error) {
      console.error('Error getting attendance by date range:', error);
      return [];
    }
  }

  async addAttendance(attendanceData: Omit<Attendance, 'id' | 'createdAt' | 'updatedAt'>): Promise<Attendance> {
    try {
      const allAttendance = await this.getAllAttendance();
      const newAttendance: Attendance = {
        ...attendanceData,
        id: Date.now().toString(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      
      allAttendance.push(newAttendance);
      await AsyncStorage.setItem(ATTENDANCE_KEY, JSON.stringify(allAttendance));
      
      return newAttendance;
    } catch (error) {
      console.error('Error adding attendance:', error);
      throw error;
    }
  }

  async updateAttendance(id: string, updates: Partial<Omit<Attendance, 'id' | 'createdAt'>>): Promise<Attendance | null> {
    try {
      const allAttendance = await this.getAllAttendance();
      const index = allAttendance.findIndex(record => record.id === id);
      
      if (index === -1) {
        throw new Error('Attendance record not found');
      }
      
      allAttendance[index] = {
        ...allAttendance[index],
        ...updates,
        updatedAt: new Date().toISOString(),
      };
      
      await AsyncStorage.setItem(ATTENDANCE_KEY, JSON.stringify(allAttendance));
      return allAttendance[index];
    } catch (error) {
      console.error('Error updating attendance:', error);
      throw error;
    }
  }

  async deleteAttendance(id: string): Promise<boolean> {
    try {
      const allAttendance = await this.getAllAttendance();
      const filteredAttendance = allAttendance.filter(record => record.id !== id);
      
      if (filteredAttendance.length === allAttendance.length) {
        throw new Error('Attendance record not found');
      }
      
      await AsyncStorage.setItem(ATTENDANCE_KEY, JSON.stringify(filteredAttendance));
      return true;
    } catch (error) {
      console.error('Error deleting attendance:', error);
      throw error;
    }
  }

  async getAttendanceStats(startDate: string, endDate: string): Promise<{
    totalDays: number;
    totalPresent: number;
    totalAbsent: number;
    attendanceRate: number;
  }> {
    try {
      const attendance = await this.getAttendanceByDateRange(startDate, endDate);
      const totalDays = attendance.length;
      const totalPresent = attendance.filter(record => record.present).length;
      const totalAbsent = totalDays - totalPresent;
      const attendanceRate = totalDays > 0 ? (totalPresent / totalDays) * 100 : 0;
      
      return {
        totalDays,
        totalPresent,
        totalAbsent,
        attendanceRate,
      };
    } catch (error) {
      console.error('Error getting attendance stats:', error);
      return {
        totalDays: 0,
        totalPresent: 0,
        totalAbsent: 0,
        attendanceRate: 0,
      };
    }
  }

  async clearAllAttendance(): Promise<void> {
    try {
      await AsyncStorage.removeItem(ATTENDANCE_KEY);
    } catch (error) {
      console.error('Error clearing attendance:', error);
      throw error;
    }
  }
}

export const attendanceService = new AttendanceService();