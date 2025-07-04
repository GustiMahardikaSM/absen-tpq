import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Modal,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Plus, Search, User, BookOpen, CreditCard as Edit3, Trash2, Users } from 'lucide-react-native';
import { Student, studentService } from '@/services/studentService';
import { attendanceService } from '@/services/attendanceService';
import DatePicker from '@/components/DatePicker';
import ReadingPositionInput from '@/components/ReadingPositionInput';

export default function Students() {
  const [students, setStudents] = useState<Student[]>([]);
  const [filteredStudents, setFilteredStudents] = useState<Student[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingStudent, setEditingStudent] = useState<Student | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    name: '',
    birthDate: '',
    gender: 'Laki-laki' as 'Laki-laki' | 'Perempuan',
    readingLevel: 'Iqro 1',
    currentPosition: '',
    notes: '',
  });

  const loadStudents = async () => {
    try {
      const data = await studentService.getAllStudents();
      setStudents(data);
      setFilteredStudents(data);
    } catch (error) {
      console.error('Error loading students:', error);
    }
  };

  useEffect(() => {
    loadStudents();
  }, []);

  useEffect(() => {
    const filtered = students.filter(student =>
      student.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      student.readingLevel.toLowerCase().includes(searchQuery.toLowerCase())
    );
    setFilteredStudents(filtered);
  }, [searchQuery, students]);

  const resetForm = () => {
    setFormData({
      name: '',
      birthDate: '',
      gender: 'Laki-laki',
      readingLevel: 'Iqro 1',
      currentPosition: '',
      notes: '',
    });
    setEditingStudent(null);
  };

  const handleAddStudent = () => {
    setShowAddModal(true);
    resetForm();
  };

  const handleEditStudent = (student: Student) => {
    setFormData({
      name: student.name,
      birthDate: student.birthDate,
      gender: student.gender,
      readingLevel: student.readingLevel,
      currentPosition: student.currentPosition,
      notes: student.notes || '',
    });
    setEditingStudent(student);
    setShowAddModal(true);
  };

  const handleDeleteStudent = (student: Student) => {
    Alert.alert(
      'Hapus Siswa',
      `Apakah Anda yakin ingin menghapus data ${student.name}? Semua data absensi siswa ini juga akan dihapus.`,
      [
        { text: 'Batal', style: 'cancel' },
        {
          text: 'Hapus',
          style: 'destructive',
          onPress: async () => {
            try {
              console.log('Deleting student:', student.id);
              
              // Delete all attendance records for this student
              const attendanceRecords = await attendanceService.getAttendanceByStudent(student.id);
              console.log('Found attendance records:', attendanceRecords.length);
              
              for (const record of attendanceRecords) {
                await attendanceService.deleteAttendance(record.id);
              }
              
              // Delete student
              const deleteResult = await studentService.deleteStudent(student.id);
              console.log('Delete result:', deleteResult);
              
              if (deleteResult) {
                await loadStudents();
                Alert.alert('Berhasil', 'Data siswa dan riwayat absensi berhasil dihapus');
              } else {
                Alert.alert('Error', 'Gagal menghapus data siswa');
              }
            } catch (error) {
              console.error('Error deleting student:', error);
              Alert.alert('Error', 'Gagal menghapus data siswa: ' + error);
            }
          },
        },
      ]
    );
  };

  const handleSaveStudent = async () => {
    if (!formData.name.trim()) {
      Alert.alert('Error', 'Nama siswa harus diisi');
      return;
    }

    try {
      if (editingStudent) {
        await studentService.updateStudent(editingStudent.id, formData);
      } else {
        await studentService.addStudent(formData);
      }
      
      setShowAddModal(false);
      resetForm();
      loadStudents();
    } catch (error) {
      Alert.alert('Error', 'Gagal menyimpan data siswa');
    }
  };

  const calculateAge = (birthDate: string) => {
    if (!birthDate) return '';
    
    const birth = new Date(birthDate);
    const today = new Date();
    
    let years = today.getFullYear() - birth.getFullYear();
    let months = today.getMonth() - birth.getMonth();
    let days = today.getDate() - birth.getDate();
    
    if (days < 0) {
      months--;
      days += new Date(today.getFullYear(), today.getMonth(), 0).getDate();
    }
    
    if (months < 0) {
      years--;
      months += 12;
    }
    
    return `${years} tahun ${months} bulan`;
  };

  const StudentCard = ({ student }: { student: Student }) => (
    <View style={styles.studentCard}>
      <View style={styles.studentHeader}>
        <View style={styles.avatarContainer}>
          <User size={24} color="#FFFFFF" />
        </View>
        <View style={styles.studentInfo}>
          <Text style={styles.studentName}>{student.name}</Text>
          <Text style={styles.studentAge}>
            {calculateAge(student.birthDate)} • {student.gender}
          </Text>
        </View>
        <View style={styles.actionButtons}>
          <TouchableOpacity 
            style={[styles.actionButton, styles.editButton]}
            onPress={() => handleEditStudent(student)}
          >
            <Edit3 size={16} color="#3B82F6" />
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.actionButton, styles.deleteButton]}
            onPress={() => handleDeleteStudent(student)}
            activeOpacity={0.7}
          >
            <Trash2 size={16} color="#EF4444" />
          </TouchableOpacity>
        </View>
      </View>
      
      <View style={styles.studentDetails}>
        <View style={styles.detailRow}>
          <BookOpen size={16} color="#22C55E" />
          <Text style={styles.detailText}>
            {student.readingLevel} - {student.currentPosition}
          </Text>
        </View>
        
        {student.notes && student.notes.length > 0 && (
          <View style={styles.detailRow}>
            <Text style={styles.notesText}>{student.notes}</Text>
          </View>
        )}
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Data Siswa TPQ</Text>
        <TouchableOpacity style={styles.addButton} onPress={handleAddStudent}>
          <Plus size={20} color="#FFFFFF" />
          <Text style={styles.addButtonText}>Tambah</Text>
        </TouchableOpacity>
      </View>

      {/* Search */}
      <View style={styles.searchContainer}>
        <Search size={20} color="#64748B" />
        <TextInput
          style={styles.searchInput}
          placeholder="Cari nama atau tingkat bacaan..."
          value={searchQuery}
          onChangeText={setSearchQuery}
        />
      </View>

      {/* Students List */}
      <ScrollView style={styles.studentsList}>
        {filteredStudents.map((student) => (
          <StudentCard key={student.id} student={student} />
        ))}
        
        {filteredStudents.length === 0 && (
          <View style={styles.emptyState}>
            <Users size={48} color="#CBD5E1" />
            <Text style={styles.emptyText}>
              {searchQuery ? 'Tidak ada siswa yang ditemukan' : 'Belum ada data siswa'}
            </Text>
          </View>
        )}
      </ScrollView>

      {/* Add/Edit Modal */}
      <Modal
        visible={showAddModal}
        animationType="slide"
        presentationStyle="pageSheet"
      >
        <SafeAreaView style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <TouchableOpacity onPress={() => setShowAddModal(false)}>
              <Text style={styles.cancelButton}>Batal</Text>
            </TouchableOpacity>
            <Text style={styles.modalTitle}>
              {editingStudent ? 'Edit Siswa' : 'Tambah Siswa'}
            </Text>
            <TouchableOpacity onPress={handleSaveStudent}>
              <Text style={styles.saveButton}>Simpan</Text>
            </TouchableOpacity>
          </View>

          <ScrollView style={styles.formContainer}>
            <View style={styles.formGroup}>
              <Text style={styles.label}>Nama Lengkap *</Text>
              <TextInput
                style={styles.input}
                value={formData.name}
                onChangeText={(text) => setFormData({...formData, name: text})}
                placeholder="Masukkan nama lengkap"
              />
            </View>

            <View style={styles.formGroup}>
              <Text style={styles.label}>Tanggal Lahir</Text>
              <DatePicker
                value={formData.birthDate}
                onDateChange={(date) => setFormData({...formData, birthDate: date})}
                placeholder="Pilih tanggal lahir"
              />
            </View>

            <View style={styles.formGroup}>
              <Text style={styles.label}>Jenis Kelamin</Text>
              <View style={styles.genderContainer}>
                {['Laki-laki', 'Perempuan'].map((gender) => (
                  <TouchableOpacity
                    key={gender}
                    style={[
                      styles.genderOption,
                      formData.gender === gender && styles.genderOptionSelected
                    ]}
                    onPress={() => setFormData({...formData, gender: gender as any})}
                  >
                    <Text style={[
                      styles.genderText,
                      formData.gender === gender && styles.genderTextSelected
                    ]}>
                      {gender}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>

            <View style={styles.formGroup}>
              <Text style={styles.label}>Tingkat Bacaan</Text>
              <View style={styles.levelContainer}>
                {['Iqro 1', 'Iqro 2', 'Iqro 3', 'Iqro 4', 'Iqro 5', 'Iqro 6', 'Al-Quran'].map((level) => (
                  <TouchableOpacity
                    key={level}
                    style={[
                      styles.levelOption,
                      formData.readingLevel === level && styles.levelOptionSelected
                    ]}
                    onPress={() => setFormData({...formData, readingLevel: level, currentPosition: ''})}
                  >
                    <Text style={[
                      styles.levelText,
                      formData.readingLevel === level && styles.levelTextSelected
                    ]}>
                      {level}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>

            <View style={styles.formGroup}>
              <Text style={styles.label}>Posisi Terakhir</Text>
              <ReadingPositionInput
                readingLevel={formData.readingLevel}
                value={formData.currentPosition}
                onPositionChange={(position) => setFormData({...formData, currentPosition: position})}
                placeholder={
                  formData.readingLevel.startsWith('Iqro') 
                    ? `Pilih halaman ${formData.readingLevel}`
                    : 'Pilih surat dan ayat'
                }
              />
            </View>

            <View style={styles.formGroup}>
              <Text style={styles.label}>Catatan</Text>
              <TextInput
                style={[styles.input, styles.textArea]}
                value={formData.notes}
                onChangeText={(text) => setFormData({...formData, notes: text})}
                placeholder="Catatan tambahan (opsional)"
                multiline
                numberOfLines={3}
              />
            </View>
          </ScrollView>
        </SafeAreaView>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  addButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#22C55E',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    gap: 4,
  },
  addButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 14,
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    marginHorizontal: 20,
    marginVertical: 16,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    gap: 12,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    color: '#1E293B',
  },
  studentsList: {
    flex: 1,
    paddingHorizontal: 20,
  },
  studentCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  studentHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    gap: 12,
  },
  avatarContainer: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#22C55E',
    alignItems: 'center',
    justifyContent: 'center',
  },
  studentInfo: {
    flex: 1,
  },
  studentName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
  },
  studentAge: {
    fontSize: 14,
    color: '#64748B',
  },
  actionButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  actionButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  editButton: {
    backgroundColor: '#EFF6FF',
  },
  deleteButton: {
    backgroundColor: '#FEF2F2',
  },
  studentDetails: {
    paddingHorizontal: 16,
    paddingBottom: 16,
    gap: 8,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  detailText: {
    fontSize: 14,
    color: '#475569',
  },
  notesText: {
    fontSize: 14,
    color: '#64748B',
    fontStyle: 'italic',
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 48,
  },
  emptyText: {
    fontSize: 16,
    color: '#64748B',
    marginTop: 16,
    textAlign: 'center',
  },
  modalContainer: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
  },
  cancelButton: {
    color: '#64748B',
    fontSize: 16,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  saveButton: {
    color: '#22C55E',
    fontSize: 16,
    fontWeight: '600',
  },
  formContainer: {
    flex: 1,
    padding: 20,
  },
  formGroup: {
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
    marginBottom: 8,
  },
  input: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 16,
    color: '#1E293B',
  },
  textArea: {
    height: 80,
    textAlignVertical: 'top',
  },
  genderContainer: {
    flexDirection: 'row',
    gap: 12,
  },
  genderOption: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingVertical: 12,
    alignItems: 'center',
  },
  genderOptionSelected: {
    borderColor: '#22C55E',
    backgroundColor: '#F0FDF4',
  },
  genderText: {
    fontSize: 16,
    color: '#64748B',
  },
  genderTextSelected: {
    color: '#22C55E',
    fontWeight: '600',
  },
  levelContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  levelOption: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  levelOptionSelected: {
    borderColor: '#22C55E',
    backgroundColor: '#F0FDF4',
  },
  levelText: {
    fontSize: 14,
    color: '#64748B',
  },
  levelTextSelected: {
    color: '#22C55E',
    fontWeight: '600',
  },
});