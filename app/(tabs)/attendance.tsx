import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Modal,
  Alert,
  TextInput,
  Switch,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Calendar, CircleCheck as CheckCircle, Circle as XCircle, User, BookOpen, Star, MessageSquare } from 'lucide-react-native';
import { Student, studentService } from '@/services/studentService';
import { Attendance, attendanceService } from '@/services/attendanceService';
import AsyncStorage from '@react-native-async-storage/async-storage';
import ReadingPositionInput from '@/components/ReadingPositionInput';

// Tambahkan tipe StudentWithUrut untuk urutan
type StudentWithUrut = Student & { urut?: number };

export default function AttendanceScreen() {
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [attendanceData, setAttendanceData] = useState<Attendance[]>([]);
  const [showAttendanceModal, setShowAttendanceModal] = useState(false);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);

  // Form state for attendance
  const [attendanceForm, setAttendanceForm] = useState({
    present: true,
    readingType: 'Iqro' as 'Iqro' | 'Al-Quran',
    currentReading: '',
    lessonCompleted: false,
    teacherNotes: '',
    currentIqro: '',
  });

  // Ubah default: jika belum dipencet, asumsikan TIDAK ADA kegiatan TPQ
  const [hasActivityToday, setHasActivityToday] = useState<boolean | null>(false);
  const [showActivityPrompt, setShowActivityPrompt] = useState(false);

  const loadStudents = async () => {
    try {
      const data = await studentService.getAllStudents();
      setStudents(data);
    } catch (error) {
      console.error('Error loading students:', error);
    }
  };

  const loadAttendance = async (date: string) => {
    try {
      const data = await attendanceService.getAttendanceByDate(date);
      setAttendanceData(data);
    } catch (error) {
      console.error('Error loading attendance:', error);
    }
  };

  useEffect(() => {
    loadStudents();
  }, []);

  useEffect(() => {
    loadAttendance(selectedDate);
  }, [selectedDate]);

  // Ganti useEffect untuk showActivityPrompt agar hanya muncul sekali per tanggal
  useEffect(() => {
    const checkPrompt = async () => {
      const key = `tpq-activity-${selectedDate}`;
      const value = await AsyncStorage.getItem(key);
      if (value === null) {
        setHasActivityToday(false); // default: tidak ada kegiatan
        setShowActivityPrompt(true);
      } else {
        setHasActivityToday(value === 'true');
        setShowActivityPrompt(false);
      }
    };
    checkPrompt();
  }, [selectedDate]);

  const getAttendanceForStudent = (studentId: string) => {
    return attendanceData.find(a => a.studentId === studentId);
  };

  const handleMarkAttendance = (student: Student) => {
    const existingAttendance = getAttendanceForStudent(student.id);
    
    setSelectedStudent(student);
    setAttendanceForm({
      present: existingAttendance?.present ?? true,
      readingType: student.readingLevel.startsWith('Iqro') ? 'Iqro' : 'Al-Quran',
      currentReading: existingAttendance?.currentReading ?? student.currentPosition,
      lessonCompleted: existingAttendance?.lessonCompleted ?? false,
      teacherNotes: existingAttendance?.teacherNotes ?? '',
      currentIqro: student.readingLevel,
    });
    setShowAttendanceModal(true);
  };

  // Fungsi untuk handle jawaban prompt
  const handleActivityPrompt = async (hasActivity: boolean) => {
    setHasActivityToday(hasActivity);
    setShowActivityPrompt(false);
    const key = `tpq-activity-${selectedDate}`;
    await AsyncStorage.setItem(key, hasActivity ? 'true' : 'false');
    if (hasActivity) {
      // Simpan absensi tidak hadir ke storage untuk semua siswa yang belum dicatat
      const attendance = await attendanceService.getAttendanceByDate(selectedDate);
      const absentStudents = students.filter(s => !attendance.find(a => a.studentId === s.id));
      for (const s of absentStudents) {
        await attendanceService.addAttendance({
          studentId: s.id,
          studentName: s.name,
          date: selectedDate,
          present: false,
          readingType: (s.readingLevel && s.readingLevel.startsWith('Iqro')) ? 'Iqro' : 'Al-Quran',
          currentReading: s.currentPosition,
          lessonCompleted: false,
          teacherNotes: ''
        });
      }
      loadAttendance(selectedDate);
    }
  };

  // Otomatis simpan absensi tidak hadir ke storage jika ada kegiatan TPQ dan data siswa sudah ada
  useEffect(() => {
    const simpanTidakHadirOtomatis = async () => {
      if (hasActivityToday && students.length > 0) {
        const attendance = await attendanceService.getAttendanceByDate(selectedDate);
        const absentStudents = students.filter(s => !attendance.find(a => a.studentId === s.id));
        for (const s of absentStudents) {
          await attendanceService.addAttendance({
            studentId: s.id,
            studentName: s.name,
            date: selectedDate,
            present: false,
            readingType: (s.readingLevel && s.readingLevel.startsWith('Iqro')) ? 'Iqro' : 'Al-Quran',
            currentReading: s.currentPosition,
            lessonCompleted: false,
            teacherNotes: ''
          });
        }
        loadAttendance(selectedDate);
      }
    };
    simpanTidakHadirOtomatis();
  }, [hasActivityToday, students, selectedDate]);

  const saveAttendance = async () => {
    console.log('Tombol Simpan ditekan');
    Alert.alert('Info', 'Tombol Simpan ditekan');
    if (!selectedStudent) {
      console.log('Tidak ada siswa yang dipilih');
      Alert.alert('Error', 'Tidak ada siswa yang dipilih.');
      return;
    }
    try {
      const attendanceRecord: Omit<Attendance, 'id'> = {
        studentId: selectedStudent.id,
        studentName: selectedStudent.name,
        date: selectedDate,
        present: attendanceForm.present,
        readingType: attendanceForm.readingType,
        currentReading: attendanceForm.currentReading,
        lessonCompleted: attendanceForm.lessonCompleted,
        teacherNotes: attendanceForm.teacherNotes,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      console.log('attendanceRecord', attendanceRecord);
      const existingAttendance = getAttendanceForStudent(selectedStudent.id);
      console.log('existingAttendance', existingAttendance);
      if (existingAttendance && !existingAttendance.id.startsWith('dummy-')) {
        console.log('Memanggil updateAttendance');
        Alert.alert('Info', 'Memanggil updateAttendance');
        await attendanceService.updateAttendance(existingAttendance.id, attendanceRecord);
      } else {
        console.log('Memanggil addAttendance');
        Alert.alert('Info', 'Memanggil addAttendance');
        await attendanceService.addAttendance(attendanceRecord);
      }
      if (attendanceForm.present) {
        let newReadingLevel = attendanceForm.currentIqro || selectedStudent.readingLevel;
        if (selectedStudent.readingLevel.startsWith('Iqro') && attendanceForm.readingType === 'Al-Quran') {
          newReadingLevel = 'Al-Quran';
        } else if (selectedStudent.readingLevel === 'Al-Quran' && attendanceForm.readingType === 'Iqro') {
          newReadingLevel = attendanceForm.currentIqro || 'Iqro 1';
        }
        console.log('Update student position', newReadingLevel);
        await studentService.updateStudent(selectedStudent.id, {
          currentPosition: attendanceForm.readingType === 'Iqro'
            ? `Halaman ${attendanceForm.currentReading}`
            : attendanceForm.currentReading,
          readingLevel: newReadingLevel,
        });
      }
      setShowAttendanceModal(false);
      loadAttendance(selectedDate);
      loadStudents();
      Alert.alert('Sukses', 'Absensi berhasil disimpan!');
    } catch (error) {
      console.log('Error saat menyimpan absensi', error);
      Alert.alert('Error', 'Gagal menyimpan data absensi: ' + error);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('id-ID', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const StudentAttendanceCard = ({ student, disabled }: { student: StudentWithUrut, disabled: boolean }) => {
    const attendance = getAttendanceForStudent(student.id);
    const isPresent = attendance?.present;
    const hasAttendance = attendance !== undefined;

    return (
      <TouchableOpacity 
        style={[styles.studentCard, disabled && { opacity: 0.5 }]}
        onPress={disabled ? undefined : () => handleMarkAttendance(student)}
        disabled={disabled}
      >
        <View style={styles.studentHeader}>
          <View style={[
            styles.avatarContainer,
            { backgroundColor: hasAttendance ? (isPresent ? '#22C55E' : '#EF4444') : '#CBD5E1' }
          ]}>
            <User size={24} color="#FFFFFF" />
          </View>
          
          <View style={styles.studentInfo}>
            <Text style={styles.studentName}>{student.urut ? `${student.urut}. ` : ''}{student.name}</Text>
            <Text style={styles.studentLevel}>{student.readingLevel}</Text>
            <Text style={styles.studentPosition}>{student.currentPosition}</Text>
          </View>

          <View style={styles.statusContainer}>
            {hasAttendance ? (
              <View style={styles.statusBadge}>
                {isPresent ? (
                  <CheckCircle size={20} color="#22C55E" />
                ) : (
                  <XCircle size={20} color="#EF4444" />
                )}
                <Text style={[
                  styles.statusText,
                  { color: isPresent ? '#22C55E' : '#EF4444' }
                ]}>
                  {isPresent ? 'Hadir' : 'Tidak Hadir'}
                </Text>
              </View>
            ) : (
              <Text style={styles.noStatusText}>Belum Dicatat</Text>
            )}
          </View>
        </View>

        {attendance && attendance.present && (
          <View style={styles.attendanceDetails}>
            <View style={styles.detailRow}>
              <BookOpen size={16} color="#3B82F6" />
              <Text style={styles.detailText}>
                {attendance.readingType}: {attendance.currentReading}
              </Text>
              {attendance.lessonCompleted && (
                <Star size={16} color="#F59E0B" />
              )}
            </View>
            
            {attendance.teacherNotes && (
              <View style={styles.detailRow}>
                <MessageSquare size={16} color="#8B5CF6" />
                <Text style={styles.notesText}>{attendance.teacherNotes}</Text>
              </View>
            )}
          </View>
        )}
      </TouchableOpacity>
    );
  };

  const presentCount = attendanceData.filter(a => a.present).length;
  const absentCount = attendanceData.filter(a => !a.present).length;
  const completedCount = attendanceData.filter(a => a.lessonCompleted).length;

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Absensi Siswa</Text>
        <TouchableOpacity style={styles.dateButton}>
          <Calendar size={20} color="#FFFFFF" />
          <Text style={styles.dateButtonText}>Pilih Tanggal</Text>
        </TouchableOpacity>
      </View>

      {/* Selected Date */}
      <View style={styles.dateContainer}>
        <Text style={styles.selectedDate}>{formatDate(selectedDate)}</Text>
      </View>

      {/* Toggle Manual */}
      <View style={{ flexDirection: 'row', alignItems: 'center', marginVertical: 8, gap: 8 }}>
        <Text style={{ fontSize: 16 }}>Ada Kegiatan TPQ Hari Ini?</Text>
        <Switch
          value={!!hasActivityToday}
          onValueChange={async (val) => {
            setHasActivityToday(val);
            const key = `tpq-activity-${selectedDate}`;
            await AsyncStorage.setItem(key, val ? 'true' : 'false');
          }}
        />
        <Text style={{ fontSize: 16, color: hasActivityToday ? '#22C55E' : '#EF4444', marginLeft: 8 }}>
          {hasActivityToday ? 'Ya' : 'Tidak'}
        </Text>
      </View>

      {/* Statistics */}
      <View style={styles.statsContainer}>
        <View style={styles.statItem}>
          <CheckCircle size={20} color="#22C55E" />
          <Text style={styles.statNumber}>{presentCount}</Text>
          <Text style={styles.statLabel}>Hadir</Text>
        </View>
        <View style={styles.statItem}>
          <XCircle size={20} color="#EF4444" />
          <Text style={styles.statNumber}>{absentCount}</Text>
          <Text style={styles.statLabel}>Tidak Hadir</Text>
        </View>
        <View style={styles.statItem}>
          <Star size={20} color="#F59E0B" />
          <Text style={styles.statNumber}>{completedCount}</Text>
          <Text style={styles.statLabel}>Lulus</Text>
        </View>
      </View>

      {/* Students List */}
      {hasActivityToday ? (
        <ScrollView style={styles.studentsList}>
          {students.map((student, idx) => (
            <StudentAttendanceCard
              key={student.id}
              student={{ ...student, urut: idx + 1 } as StudentWithUrut}
              disabled={false}
            />
          ))}
          {students.length === 0 && (
            <View style={styles.emptyState}>
              <User size={48} color="#CBD5E1" />
              <Text style={styles.emptyText}>Belum ada data siswa</Text>
            </View>
          )}
        </ScrollView>
      ) : (
        <View style={styles.emptyState}>
          <User size={48} color="#CBD5E1" />
          <Text style={styles.emptyText}>tekan switch di atas untuk memulai pembelajaran dan membuka absensi</Text>
        </View>
      )}

      {/* Attendance Modal */}
      <Modal
        visible={showAttendanceModal}
        animationType="slide"
        presentationStyle="pageSheet"
      >
        <SafeAreaView style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <TouchableOpacity onPress={() => setShowAttendanceModal(false)}>
              <Text style={styles.cancelButton}>Batal</Text>
            </TouchableOpacity>
            <Text style={styles.modalTitle}>Catat Absensi</Text>
            {/* Ubah tombol Simpan agar SELALU BISA DIPENCET */}
            <TouchableOpacity onPress={saveAttendance}>
              <Text style={styles.saveButton}>Simpan</Text>
            </TouchableOpacity>
          </View>

          {selectedStudent && (
            <ScrollView style={styles.formContainer}>
              <View style={styles.studentPreview}>
                <Text style={styles.studentPreviewName}>{selectedStudent.name}</Text>
                <Text style={styles.studentPreviewLevel}>{selectedStudent.readingLevel}</Text>
              </View>

              {/* Kehadiran */}
              <View style={styles.formGroup}>
                <Text style={styles.label}>Kehadiran</Text>
                <View style={styles.attendanceOptions}>
                  <TouchableOpacity
                    style={[
                      styles.attendanceOption,
                      attendanceForm.present && styles.attendanceOptionSelected
                    ]}
                    onPress={() => setAttendanceForm({...attendanceForm, present: true})}
                  >
                    <CheckCircle size={20} color={attendanceForm.present ? '#FFFFFF' : '#22C55E'} />
                    <Text style={[
                      styles.attendanceOptionText,
                      attendanceForm.present && styles.attendanceOptionTextSelected
                    ]}>
                      Hadir
                    </Text>
                  </TouchableOpacity>
                  
                  <TouchableOpacity
                    style={[
                      styles.attendanceOption,
                      !attendanceForm.present && styles.attendanceOptionAbsent
                    ]}
                    onPress={() => setAttendanceForm({...attendanceForm, present: false})}
                  >
                    <XCircle size={20} color={!attendanceForm.present ? '#FFFFFF' : '#EF4444'} />
                    <Text style={[
                      styles.attendanceOptionText,
                      !attendanceForm.present && styles.attendanceOptionTextAbsent
                    ]}>
                      Tidak Hadir
                    </Text>
                  </TouchableOpacity>
                </View>
              </View>

              {attendanceForm.present && (
                <>
                  {/* Jenis Bacaan & Kategori Iqro/Alquran */}
                  <View style={styles.formGroup}>
                    <Text style={styles.label}>Jenis Bacaan</Text>
                    <View style={styles.readingTypeOptions}>
                      {['Iqro', 'Al-Quran'].map((type) => (
                        <TouchableOpacity
                          key={type}
                          style={[
                            styles.readingTypeOption,
                            attendanceForm.readingType === type && styles.readingTypeOptionSelected
                          ]}
                          onPress={() => setAttendanceForm({...attendanceForm, readingType: type as any})}
                        >
                          <Text style={[
                            styles.readingTypeText,
                            attendanceForm.readingType === type && styles.readingTypeTextSelected
                          ]}>
                            {type}
                          </Text>
                        </TouchableOpacity>
                      ))}
                    </View>
                    {/* Dropdown kategori Iqro/Alquran */}
                    {attendanceForm.readingType === 'Iqro' && (
                      <View style={{ flexDirection: 'row', marginTop: 8, flexWrap: 'wrap', gap: 8 }}>
                        {[1,2,3,4,5,6].map((num) => (
                          <TouchableOpacity
                            key={num}
                            style={{
                              backgroundColor: attendanceForm.currentIqro === `Iqro ${num}` ? '#3B82F6' : '#E5E7EB',
                              paddingHorizontal: 12,
                              paddingVertical: 6,
                              borderRadius: 8,
                              marginRight: 6,
                              marginBottom: 6,
                            }}
                            onPress={() => setAttendanceForm({ ...attendanceForm, currentIqro: `Iqro ${num}` })}
                          >
                            <Text style={{ color: attendanceForm.currentIqro === `Iqro ${num}` ? 'white' : '#1E293B' }}>{`Iqro ${num}`}</Text>
                          </TouchableOpacity>
                        ))}
                      </View>
                    )}
                    {attendanceForm.readingType === 'Al-Quran' && (
                      <TouchableOpacity
                        style={{
                          backgroundColor: attendanceForm.currentIqro === 'Al-Quran' ? '#3B82F6' : '#E5E7EB',
                          paddingHorizontal: 12,
                          paddingVertical: 6,
                          borderRadius: 8,
                          marginTop: 8,
                        }}
                        onPress={() => setAttendanceForm({ ...attendanceForm, currentIqro: 'Al-Quran' })}
                      >
                        <Text style={{ color: attendanceForm.currentIqro === 'Al-Quran' ? 'white' : '#1E293B' }}>Al-Quran</Text>
                      </TouchableOpacity>
                    )}
                  </View>

                  {/* Input posisi bacaan */}
                  <View style={styles.formGroup}>
                    {attendanceForm.readingType === 'Iqro' ? (
                      <>
                        <Text style={styles.label}>Halaman Bacaan Hari Ini</Text>
                        <TextInput
                          style={styles.input}
                          value={attendanceForm.currentReading.replace(/\D/g, '')}
                          onChangeText={(text) => setAttendanceForm({ ...attendanceForm, currentReading: text.replace(/\D/g, '') })}
                          placeholder="Masukkan nomor halaman"
                          keyboardType="numeric"
                          maxLength={3}
                        />
                      </>
                    ) : (
                      <>
                        <Text style={styles.label}>Posisi Bacaan Hari Ini</Text>
                        <ReadingPositionInput
                          readingLevel={'Al-Quran'}
                          value={attendanceForm.currentReading}
                          onPositionChange={(val) => setAttendanceForm({ ...attendanceForm, currentReading: val })}
                          placeholder="Pilih surah dan ayat"
                        />
                      </>
                    )}
                  </View>

                  {/* Lulus/Tidak */}
                  <View style={styles.formGroup}>
                    <TouchableOpacity
                      style={[
                        styles.completionOption,
                        attendanceForm.lessonCompleted && styles.completionOptionSelected
                      ]}
                      onPress={() => setAttendanceForm({
                        ...attendanceForm, 
                        lessonCompleted: !attendanceForm.lessonCompleted
                      })}
                    >
                      <Star size={20} color={attendanceForm.lessonCompleted ? '#FFFFFF' : '#F59E0B'} />
                      <Text style={[
                        styles.completionOptionText,
                        attendanceForm.lessonCompleted && styles.completionOptionTextSelected
                      ]}>
                        Lulus / Naik Level
                      </Text>
                    </TouchableOpacity>
                    {/* Tombol Ulang/Tidak Lulus */}
                    <TouchableOpacity
                      style={[
                        styles.completionOption,
                        !attendanceForm.lessonCompleted && { backgroundColor: '#EF4444' }
                      ]}
                      onPress={() => setAttendanceForm({
                        ...attendanceForm,
                        lessonCompleted: false
                      })}
                    >
                      <Text style={[
                        styles.completionOptionText,
                        !attendanceForm.lessonCompleted && { color: 'white' }
                      ]}>
                        Ulang / Tidak Lulus
                      </Text>
                    </TouchableOpacity>
                  </View>

                  {/* Catatan Guru */}
                  <View style={styles.formGroup}>
                    <Text style={styles.label}>Catatan Guru</Text>
                    <TextInput
                      style={[styles.input, styles.textArea]}
                      value={attendanceForm.teacherNotes}
                      onChangeText={(text) => setAttendanceForm({...attendanceForm, teacherNotes: text})}
                      placeholder="Catatan tambahan tentang perkembangan siswa..."
                      multiline
                      numberOfLines={3}
                    />
                  </View>
                </>
              )}
            </ScrollView>
          )}
        </SafeAreaView>
      </Modal>

      {/* Tambahkan Modal untuk menanyakan kegiatan TPQ hari ini */}
      <Modal
        visible={showActivityPrompt}
        transparent
        animationType="fade"
      >
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: 'rgba(0,0,0,0.3)' }}>
          <View style={{ backgroundColor: 'white', padding: 24, borderRadius: 12, alignItems: 'center', width: 300 }}>
            <Text style={{ fontSize: 18, fontWeight: 'bold', marginBottom: 12 }}>Konfirmasi Kegiatan TPQ</Text>
            <Text style={{ fontSize: 16, marginBottom: 20 }}>Apakah hari ini ada kegiatan TPQ?</Text>
            <View style={{ flexDirection: 'row', gap: 16 }}>
              <TouchableOpacity
                style={{ backgroundColor: '#22C55E', padding: 12, borderRadius: 8, marginRight: 8 }}
                onPress={() => handleActivityPrompt(true)}
              >
                <Text style={{ color: 'white', fontWeight: 'bold' }}>Ya</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={{ backgroundColor: '#EF4444', padding: 12, borderRadius: 8 }}
                onPress={() => handleActivityPrompt(false)}
              >
                <Text style={{ color: 'white', fontWeight: 'bold' }}>Tidak</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
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
  dateButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#3B82F6',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    gap: 4,
  },
  dateButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 14,
  },
  dateContainer: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
  },
  selectedDate: {
    fontSize: 16,
    color: '#1E293B',
    textAlign: 'center',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    marginBottom: 16,
  },
  statItem: {
    alignItems: 'center',
    gap: 4,
  },
  statNumber: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  statLabel: {
    fontSize: 12,
    color: '#64748B',
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
    marginBottom: 2,
  },
  studentLevel: {
    fontSize: 14,
    color: '#3B82F6',
    marginBottom: 2,
  },
  studentPosition: {
    fontSize: 12,
    color: '#64748B',
  },
  statusContainer: {
    alignItems: 'flex-end',
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  statusText: {
    fontSize: 14,
    fontWeight: '600',
  },
  noStatusText: {
    fontSize: 12,
    color: '#94A3B8',
  },
  attendanceDetails: {
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
    flex: 1,
    fontSize: 14,
    color: '#475569',
  },
  notesText: {
    flex: 1,
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
  studentPreview: {
    backgroundColor: '#F0FDF4',
    padding: 16,
    borderRadius: 12,
    marginBottom: 20,
    alignItems: 'center',
  },
  studentPreviewName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
  },
  studentPreviewLevel: {
    fontSize: 14,
    color: '#22C55E',
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
  attendanceOptions: {
    flexDirection: 'row',
    gap: 12,
  },
  attendanceOption: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 2,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingVertical: 12,
    gap: 8,
  },
  attendanceOptionSelected: {
    backgroundColor: '#22C55E',
    borderColor: '#22C55E',
  },
  attendanceOptionAbsent: {
    backgroundColor: '#EF4444',
    borderColor: '#EF4444',
  },
  attendanceOptionText: {
    fontSize: 16,
    color: '#64748B',
    fontWeight: '600',
  },
  attendanceOptionTextSelected: {
    color: '#FFFFFF',
  },
  attendanceOptionTextAbsent: {
    color: '#FFFFFF',
  },
  readingTypeOptions: {
    flexDirection: 'row',
    gap: 12,
  },
  readingTypeOption: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingVertical: 12,
    alignItems: 'center',
  },
  readingTypeOptionSelected: {
    borderColor: '#3B82F6',
    backgroundColor: '#EFF6FF',
  },
  readingTypeText: {
    fontSize: 16,
    color: '#64748B',
  },
  readingTypeTextSelected: {
    color: '#3B82F6',
    fontWeight: '600',
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
  completionOption: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 2,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 8,
  },
  completionOptionSelected: {
    backgroundColor: '#F59E0B',
    borderColor: '#F59E0B',
  },
  completionOptionText: {
    fontSize: 16,
    color: '#64748B',
    fontWeight: '600',
  },
  completionOptionTextSelected: {
    color: '#FFFFFF',
  },
});