import { storageService } from './storageService';

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

const ATTENDANCE_KEY = 'tpq_attendance_v2';

// Generate ID dengan format YYMMDDHHmmss
const generateAttendanceId = (): string => {
  const now = new Date();
  const year = now.getFullYear().toString().slice(-2);
  const month = (now.getMonth() + 1).toString().padStart(2, '0');
  const day = now.getDate().toString().padStart(2, '0');
  const hour = now.getHours().toString().padStart(2, '0');
  const minute = now.getMinutes().toString().padStart(2, '0');
  const second = now.getSeconds().toString().padStart(2, '0');
  
  return `${year}${month}${day}${hour}${minute}${second}`;
};

class AttendanceService {
  // CREATE - Tambah data absensi
  async addAttendance(attendanceData: Omit<Attendance, 'id' | 'createdAt' | 'updatedAt'>): Promise<Attendance> {
    try {
      console.log('Adding attendance for student:', attendanceData.studentName);
      
      const allAttendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      const newAttendance: Attendance = {
        ...attendanceData,
        id: generateAttendanceId(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      
      allAttendance.push(newAttendance);
      const saved = await storageService.saveData(ATTENDANCE_KEY, allAttendance);
      
      if (!saved) {
        throw new Error('Failed to save attendance data');
      }
      
      console.log('Attendance added successfully:', newAttendance.id);
      return newAttendance;
    } catch (error) {
      console.error('Error adding attendance:', error);
      throw error;
    }
  }

  // READ - Ambil semua data absensi
  async getAllAttendance(): Promise<Attendance[]> {
    try {
      console.log('Getting all attendance...');
      const attendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      console.log(`Retrieved ${attendance.length} attendance records`);
      return attendance;
    } catch (error) {
      console.error('Error getting all attendance:', error);
      return [];
    }
  }

  // READ - Ambil absensi berdasarkan tanggal
  async getAttendanceByDate(date: string): Promise<Attendance[]> {
    try {
      console.log('Getting attendance for date:', date);
      const attendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      const results = attendance.filter(record => record.date === date);
      console.log(`Found ${results.length} attendance records for ${date}`);
      return results;
    } catch (error) {
      console.error('Error getting attendance by date:', error);
      return [];
    }
  }

  // READ - Ambil absensi berdasarkan siswa
  async getAttendanceByStudent(studentId: string): Promise<Attendance[]> {
    try {
      console.log('Getting attendance for student:', studentId);
      const attendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      const results = attendance.filter(record => record.studentId === studentId);
      console.log(`Found ${results.length} attendance records for student ${studentId}`);
      return results;
    } catch (error) {
      console.error('Error getting attendance by student:', error);
      return [];
    }
  }

  // READ - Ambil absensi berdasarkan rentang tanggal
  async getAttendanceByDateRange(startDate: string, endDate: string): Promise<Attendance[]> {
    try {
      console.log('Getting attendance for date range:', startDate, 'to', endDate);
      const attendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      const results = attendance.filter(record => 
        record.date >= startDate && record.date <= endDate
      );
      console.log(`Found ${results.length} attendance records in date range`);
      return results;
    } catch (error) {
      console.error('Error getting attendance by date range:', error);
      return [];
    }
  }

  // UPDATE - Update data absensi
  async updateAttendance(id: string, updates: Partial<Omit<Attendance, 'id' | 'createdAt'>>): Promise<Attendance | null> {
    try {
      console.log('Updating attendance:', id);
      
      const allAttendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      const index = allAttendance.findIndex(record => record.id === id);
      
      if (index === -1) {
        console.log('Attendance record not found for update:', id);
        throw new Error('Attendance record not found');
      }
      
      allAttendance[index] = {
        ...allAttendance[index],
        ...updates,
        updatedAt: new Date().toISOString(),
      };
      
      const saved = await storageService.saveData(ATTENDANCE_KEY, allAttendance);
      
      if (!saved) {
        throw new Error('Failed to save updated attendance data');
      }
      
      console.log('Attendance updated successfully:', id);
      return allAttendance[index];
    } catch (error) {
      console.error('Error updating attendance:', error);
      throw error;
    }
  }

  // DELETE - Hapus data absensi
  async deleteAttendance(id: string): Promise<boolean> {
    try {
      console.log('Deleting attendance:', id);
      
      const allAttendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      const initialCount = allAttendance.length;
      
      const filteredAttendance = allAttendance.filter(record => record.id !== id);
      
      if (filteredAttendance.length === initialCount) {
        console.log('Attendance record not found for deletion:', id);
        throw new Error('Attendance record not found');
      }
      
      const saved = await storageService.saveData(ATTENDANCE_KEY, filteredAttendance);
      
      if (!saved) {
        throw new Error('Failed to save after deletion');
      }
      
      console.log('Attendance deleted successfully:', id);
      return true;
    } catch (error) {
      console.error('Error deleting attendance:', error);
      throw error;
    }
  }

  // STATS - Statistik absensi
  async getAttendanceStats(startDate: string, endDate: string): Promise<{
    totalDays: number;
    totalPresent: number;
    totalAbsent: number;
    attendanceRate: number;
  }> {
    try {
      console.log('Calculating attendance stats for:', startDate, 'to', endDate);
      
      const attendance = await this.getAttendanceByDateRange(startDate, endDate);
      const totalDays = attendance.length;
      const totalPresent = attendance.filter(record => record.present).length;
      const totalAbsent = totalDays - totalPresent;
      const attendanceRate = totalDays > 0 ? (totalPresent / totalDays) * 100 : 0;
      
      const stats = {
        totalDays,
        totalPresent,
        totalAbsent,
        attendanceRate,
      };
      
      console.log('Attendance stats calculated:', stats);
      return stats;
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

  // IMPORT - Import data absensi dengan logika overwrite/add
  async importAttendance(importedAttendance: Attendance[]): Promise<{
    added: number;
    updated: number;
    errors: string[];
  }> {
    try {
      console.log('Importing attendance records:', importedAttendance.length);
      
      const existingAttendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      let added = 0;
      let updated = 0;
      const errors: string[] = [];
      
      for (const importedRecord of importedAttendance) {
        try {
          const existingIndex = existingAttendance.findIndex(a => a.id === importedRecord.id);
          
          if (existingIndex !== -1) {
            // ID sudah ada, overwrite data
            existingAttendance[existingIndex] = {
              ...importedRecord,
              updatedAt: new Date().toISOString(),
            };
            updated++;
            console.log('Updated attendance:', importedRecord.id);
          } else {
            // ID baru, tambahkan sebagai record baru
            const newRecord = {
              ...importedRecord,
              createdAt: importedRecord.createdAt || new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
            existingAttendance.push(newRecord);
            added++;
            console.log('Added new attendance:', importedRecord.id);
          }
        } catch (error) {
          const errorMsg = `Error processing attendance for ${importedRecord.studentName}: ${error}`;
          console.error(errorMsg);
          errors.push(errorMsg);
        }
      }
      
      const saved = await storageService.saveData(ATTENDANCE_KEY, existingAttendance);
      
      if (!saved) {
        throw new Error('Failed to save imported attendance data');
      }
      
      console.log(`Attendance import completed: ${added} added, ${updated} updated, ${errors.length} errors`);
      return { added, updated, errors };
    } catch (error) {
      console.error('Error importing attendance:', error);
      throw error;
    }
  }

  // CLEAR - Hapus semua data absensi
  async clearAllAttendance(): Promise<boolean> {
    try {
      console.log('Clearing all attendance...');
      const cleared = await storageService.clearData(ATTENDANCE_KEY);
      console.log('All attendance cleared:', cleared);
      return cleared;
    } catch (error) {
      console.error('Error clearing attendance:', error);
      return false;
    }
  }

  // BACKUP - Backup data absensi
  async backupAttendance(): Promise<Attendance[]> {
    try {
      console.log('Creating backup of attendance...');
      const attendance = await storageService.getData<Attendance>(ATTENDANCE_KEY);
      console.log(`Backed up ${attendance.length} attendance records`);
      return attendance;
    } catch (error) {
      console.error('Error backing up attendance:', error);
      return [];
    }
  }
}

export const attendanceService = new AttendanceService();