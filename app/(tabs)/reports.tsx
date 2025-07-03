import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Modal,
  Alert,
  Share,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Calendar, TrendingUp, Users, CircleCheck as CheckCircle, Circle as XCircle, Star, User, BookOpen, ChevronRight, Clock, Download, Share as ShareIcon } from 'lucide-react-native';
import { studentService, Student } from '@/services/studentService';
import { attendanceService, Attendance } from '@/services/attendanceService';

interface StudentProgress {
  student: Student;
  totalDays: number;
  presentDays: number;
  attendanceRate: number;
  completedLessons: number;
  failedLessons: number;
  currentStreak: number;
  recentNotes: string[];
  initialPosition: string;
  currentPosition: string;
}

interface DailyReport {
  date: string;
  totalStudents: number;
  presentStudents: number;
  absentStudents: number;
  completedLessons: number;
  attendanceDetails: {
    student: Student;
    attendance: Attendance | null;
  }[];
}

export default function Reports() {
  const [students, setStudents] = useState<Student[]>([]);
  const [studentProgress, setStudentProgress] = useState<StudentProgress[]>([]);
  const [dailyReports, setDailyReports] = useState<DailyReport[]>([]);
  const [selectedStudent, setSelectedStudent] = useState<StudentProgress | null>(null);
  const [selectedDailyReport, setSelectedDailyReport] = useState<DailyReport | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showDailyModal, setShowDailyModal] = useState(false);
  const [reportType, setReportType] = useState<'individual' | 'daily'>('individual');

  const loadStudentsAndProgress = async () => {
    try {
      const studentsData = await studentService.getAllStudents();
      setStudents(studentsData);

      const progressData: StudentProgress[] = [];
      
      for (const student of studentsData) {
        const attendance = await attendanceService.getAttendanceByStudent(student.id);
        
        // Calculate date range for last month
        const currentDate = new Date();
        const startDate = new Date(currentDate);
        startDate.setMonth(currentDate.getMonth() - 1);

        const filteredAttendance = attendance.filter(a => 
          new Date(a.date) >= startDate
        );

        const presentDays = filteredAttendance.filter(a => a.present).length;
        const completedLessons = filteredAttendance.filter(a => a.lessonCompleted).length;
        const failedLessons = filteredAttendance.filter(a => a.present && !a.lessonCompleted).length;

        // Calculate current streak
        const { currentStreak } = calculateStreaks(attendance);

        // Get recent notes
        const recentNotes = attendance
          .filter(a => a.teacherNotes && a.teacherNotes.trim() !== '')
          .slice(-3)
          .map(a => a.teacherNotes);

        // Get initial position (30 days ago or first record)
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
        const thirtyDaysAgoString = thirtyDaysAgo.toISOString().split('T')[0];
        
        const oldAttendance = attendance
          .filter(a => a.date <= thirtyDaysAgoString)
          .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
        
        const initialPosition = oldAttendance.length > 0 
          ? `${oldAttendance[0].readingType === 'Iqro' ? oldAttendance[0].readingType : 'Surat'} ${oldAttendance[0].currentReading}`
          : student.currentPosition;

        progressData.push({
          student,
          totalDays: filteredAttendance.length,
          presentDays,
          attendanceRate: filteredAttendance.length > 0 ? (presentDays / filteredAttendance.length) * 100 : 0,
          completedLessons,
          failedLessons,
          currentStreak,
          recentNotes,
          initialPosition,
          currentPosition: `${student.readingLevel.startsWith('Iqro') ? student.readingLevel : 'Surat'} ${student.currentPosition}`,
        });
      }

      setStudentProgress(progressData);
    } catch (error) {
      console.error('Error loading student progress:', error);
    }
  };

  const loadDailyReports = async () => {
    try {
      const studentsData = await studentService.getAllStudents();
      const reports: DailyReport[] = [];
      
      // Generate reports for last 7 days
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        const dateString = date.toISOString().split('T')[0];
        
        const dayAttendance = await attendanceService.getAttendanceByDate(dateString);
        
        const attendanceDetails = studentsData.map(student => ({
          student,
          attendance: dayAttendance.find(a => a.studentId === student.id) || null
        }));
        
        const presentStudents = dayAttendance.filter(a => a.present).length;
        const completedLessons = dayAttendance.filter(a => a.lessonCompleted).length;
        
        reports.push({
          date: dateString,
          totalStudents: studentsData.length,
          presentStudents,
          absentStudents: studentsData.length - presentStudents,
          completedLessons,
          attendanceDetails,
        });
      }
      
      setDailyReports(reports);
    } catch (error) {
      console.error('Error loading daily reports:', error);
    }
  };

  const calculateStreaks = (attendance: Attendance[]) => {
    const sortedAttendance = attendance
      .filter(a => a.present)
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());

    let currentStreak = 0;
    const today = new Date();
    let checkDate = new Date(today);

    // Calculate current streak (working backwards from today)
    for (let i = 0; i < 30; i++) {
      const dateString = checkDate.toISOString().split('T')[0];
      const hasAttendance = sortedAttendance.some(a => a.date === dateString);
      
      if (hasAttendance) {
        currentStreak++;
      } else {
        break;
      }
      
      checkDate.setDate(checkDate.getDate() - 1);
    }

    return { currentStreak };
  };

  useEffect(() => {
    if (reportType === 'individual') {
      loadStudentsAndProgress();
    } else {
      loadDailyReports();
    }
  }, [reportType]);

  const getProgressColor = (rate: number) => {
    if (rate >= 80) return '#22C55E';
    if (rate >= 60) return '#F59E0B';
    return '#EF4444';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('id-ID', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    });
  };

  const generatePDFReport = async (progress: StudentProgress) => {
    const reportContent = `
LAPORAN PERKEMBANGAN SISWA TPQ

Nama: ${progress.student.name}
Tingkat Bacaan: ${progress.student.readingLevel}
Tanggal Lahir: ${progress.student.birthDate ? formatDate(progress.student.birthDate) : '-'}
Jenis Kelamin: ${progress.student.gender}

STATISTIK KEHADIRAN (30 HARI TERAKHIR)
- Tingkat Kehadiran: ${progress.attendanceRate.toFixed(1)}%
- Total Hari: ${progress.totalDays}
- Hadir: ${progress.presentDays}
- Tidak Hadir: ${progress.totalDays - progress.presentDays}
- Streak Saat Ini: ${progress.currentStreak} hari berturut-turut

PERKEMBANGAN BACAAN
- Posisi Awal (30 hari lalu): ${progress.initialPosition}
- Posisi Saat Ini: ${progress.currentPosition}

PRESTASI
- Total Lulus: ${progress.completedLessons}
- Total Mengulang: ${progress.failedLessons}

CATATAN GURU TERBARU
${progress.recentNotes.length > 0 ? progress.recentNotes.map((note, index) => `${index + 1}. ${note}`).join('\n') : 'Tidak ada catatan'}

REKOMENDASI
${progress.attendanceRate >= 80 
  ? `${progress.student.name} menunjukkan konsistensi yang baik. Terus berikan motivasi dan tantangan yang sesuai.`
  : `${progress.student.name} perlu meningkatkan kehadiran. Pertimbangkan untuk memberikan perhatian khusus dan motivasi tambahan.`
}

${progress.currentStreak >= 7 ? `Apresiasi untuk streak kehadiran ${progress.currentStreak} hari berturut-turut!` : ''}

---
Laporan dibuat pada: ${new Date().toLocaleDateString('id-ID')}
    `;

    return reportContent;
  };

  const handleShareReport = async (progress: StudentProgress) => {
    try {
      const reportContent = await generatePDFReport(progress);
      const today = new Date();
      const dateString = `${today.getDate().toString().padStart(2, '0')}${(today.getMonth() + 1).toString().padStart(2, '0')}${today.getFullYear().toString().slice(-2)}`;
      
      await Share.share({
        message: reportContent,
        title: `Laporan_${progress.student.name.replace(/\s+/g, '_')}_${dateString}`,
      });
    } catch (error) {
      Alert.alert('Error', 'Gagal membagikan laporan');
    }
  };

  const handleDownloadReport = async (progress: StudentProgress) => {
    try {
      const reportContent = await generatePDFReport(progress);
      const today = new Date();
      const dateString = `${today.getDate().toString().padStart(2, '0')}${(today.getMonth() + 1).toString().padStart(2, '0')}${today.getFullYear().toString().slice(-2)}`;
      
      // For web platform, create a downloadable text file
      if (typeof window !== 'undefined') {
        const blob = new Blob([reportContent], { type: 'text/plain' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Laporan_${progress.student.name.replace(/\s+/g, '_')}_${dateString}.txt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        Alert.alert('Berhasil', 'Laporan berhasil diunduh');
      } else {
        Alert.alert('Info', 'Fitur unduh akan segera tersedia untuk platform mobile');
      }
    } catch (error) {
      Alert.alert('Error', 'Gagal mengunduh laporan');
    }
  };

  const StudentProgressCard = ({ progress }: { progress: StudentProgress }) => (
    <TouchableOpacity 
      style={styles.studentCard}
      onPress={() => {
        setSelectedStudent(progress);
        setShowDetailModal(true);
      }}
    >
      <View style={styles.studentHeader}>
        <View style={[
          styles.avatarContainer,
          { backgroundColor: getProgressColor(progress.attendanceRate) }
        ]}>
          <User size={24} color="#FFFFFF" />
        </View>
        
        <View style={styles.studentInfo}>
          <Text style={styles.studentName}>{progress.student.name}</Text>
          <Text style={styles.studentLevel}>{progress.student.readingLevel}</Text>
          <Text style={styles.studentPosition}>{progress.student.currentPosition}</Text>
        </View>

        <View style={styles.progressStats}>
          <Text style={[styles.attendanceRate, { color: getProgressColor(progress.attendanceRate) }]}>
            {progress.attendanceRate.toFixed(0)}%
          </Text>
          <Text style={styles.attendanceLabel}>Kehadiran</Text>
          <ChevronRight size={16} color="#CBD5E1" />
        </View>
      </View>

      <View style={styles.quickStats}>
        <View style={styles.statItem}>
          <CheckCircle size={16} color="#22C55E" />
          <Text style={styles.statText}>{progress.presentDays} Hadir</Text>
        </View>
        <View style={styles.statItem}>
          <Star size={16} color="#F59E0B" />
          <Text style={styles.statText}>{progress.completedLessons} Lulus</Text>
        </View>
        <View style={styles.statItem}>
          <TrendingUp size={16} color="#3B82F6" />
          <Text style={styles.statText}>{progress.currentStreak} Hari Berturut</Text>
        </View>
      </View>

      {/* Mini progress bar */}
      <View style={styles.progressBarContainer}>
        <View style={styles.progressBarBackground}>
          <View 
            style={[
              styles.progressBarFill,
              { 
                width: `${progress.attendanceRate}%`,
                backgroundColor: getProgressColor(progress.attendanceRate)
              }
            ]}
          />
        </View>
      </View>
    </TouchableOpacity>
  );

  const DailyReportCard = ({ report }: { report: DailyReport }) => (
    <TouchableOpacity 
      style={styles.dailyCard}
      onPress={() => {
        setSelectedDailyReport(report);
        setShowDailyModal(true);
      }}
    >
      <View style={styles.dailyHeader}>
        <View style={styles.dateContainer}>
          <Calendar size={20} color="#3B82F6" />
          <Text style={styles.dateText}>{formatDate(report.date)}</Text>
        </View>
        <ChevronRight size={16} color="#CBD5E1" />
      </View>

      <View style={styles.dailyStats}>
        <View style={styles.dailyStatItem}>
          <Users size={16} color="#64748B" />
          <Text style={styles.dailyStatText}>{report.totalStudents} Total</Text>
        </View>
        <View style={styles.dailyStatItem}>
          <CheckCircle size={16} color="#22C55E" />
          <Text style={styles.dailyStatText}>{report.presentStudents} Hadir</Text>
        </View>
        <View style={styles.dailyStatItem}>
          <XCircle size={16} color="#EF4444" />
          <Text style={styles.dailyStatText}>{report.absentStudents} Tidak Hadir</Text>
        </View>
        <View style={styles.dailyStatItem}>
          <Star size={16} color="#F59E0B" />
          <Text style={styles.dailyStatText}>{report.completedLessons} Lulus</Text>
        </View>
      </View>

      <View style={styles.attendanceRate}>
        <Text style={styles.attendanceRateText}>
          Tingkat Kehadiran: {report.totalStudents > 0 ? Math.round((report.presentStudents / report.totalStudents) * 100) : 0}%
        </Text>
      </View>
    </TouchableOpacity>
  );

  const ReportTypeSelector = () => (
    <View style={styles.reportTypeSelector}>
      {(['individual', 'daily'] as const).map((type) => (
        <TouchableOpacity
          key={type}
          style={[
            styles.reportTypeOption,
            reportType === type && styles.reportTypeOptionSelected
          ]}
          onPress={() => setReportType(type)}
        >
          <Text style={[
            styles.reportTypeOptionText,
            reportType === type && styles.reportTypeOptionTextSelected
          ]}>
            {type === 'individual' ? 'Individual' : 'Laporan TPQ'}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  const presentCount = dailyReports.reduce((acc, r) => acc + r.presentStudents, 0);
  const absentCount = dailyReports.reduce((acc, r) => acc + r.absentStudents, 0);
  const completedCount = dailyReports.reduce((acc, r) => acc + r.completedLessons, 0);

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Laporan Perkembangan</Text>
      </View>

      <ScrollView style={styles.scrollView}>
        {/* Report Type Selector */}
        <ReportTypeSelector />

        {reportType === 'individual' ? (
          <>
            {/* Summary Stats */}
            <View style={styles.summaryContainer}>
              <Text style={styles.sectionTitle}>Ringkasan Bulan Ini</Text>
              <View style={styles.summaryStats}>
                <View style={styles.summaryItem}>
                  <Users size={20} color="#3B82F6" />
                  <Text style={styles.summaryNumber}>{students.length}</Text>
                  <Text style={styles.summaryLabel}>Total Siswa</Text>
                </View>
                <View style={styles.summaryItem}>
                  <CheckCircle size={20} color="#22C55E" />
                  <Text style={styles.summaryNumber}>
                    {Math.round(studentProgress.reduce((acc, p) => acc + p.attendanceRate, 0) / studentProgress.length || 0)}%
                  </Text>
                  <Text style={styles.summaryLabel}>Rata-rata Kehadiran</Text>
                </View>
                <View style={styles.summaryItem}>
                  <Star size={20} color="#F59E0B" />
                  <Text style={styles.summaryNumber}>
                    {studentProgress.reduce((acc, p) => acc + p.completedLessons, 0)}
                  </Text>
                  <Text style={styles.summaryLabel}>Total Kelulusan</Text>
                </View>
              </View>
            </View>

            {/* Students List */}
            <View style={styles.studentsSection}>
              <Text style={styles.sectionTitle}>Perkembangan Individual</Text>
              {studentProgress.map((progress) => (
                <StudentProgressCard key={progress.student.id} progress={progress} />
              ))}
              
              {studentProgress.length === 0 && (
                <View style={styles.emptyState}>
                  <Users size={48} color="#CBD5E1" />
                  <Text style={styles.emptyText}>Belum ada data siswa</Text>
                </View>
              )}
            </View>
          </>
        ) : (
          <>
            {/* Daily Reports Summary */}
            <View style={styles.summaryContainer}>
              <Text style={styles.sectionTitle}>Laporan TPQ (7 Hari Terakhir)</Text>
              <View style={styles.summaryStats}>
                <View style={styles.summaryItem}>
                  <Clock size={20} color="#3B82F6" />
                  <Text style={styles.summaryNumber}>{dailyReports.length}</Text>
                  <Text style={styles.summaryLabel}>Hari</Text>
                </View>
                <View style={styles.summaryItem}>
                  <CheckCircle size={20} color="#22C55E" />
                  <Text style={styles.summaryNumber}>
                    {Math.round(dailyReports.reduce((acc, r) => acc + (r.totalStudents > 0 ? (r.presentStudents / r.totalStudents) * 100 : 0), 0) / dailyReports.length || 0)}%
                  </Text>
                  <Text style={styles.summaryLabel}>Rata-rata Kehadiran</Text>
                </View>
                <View style={styles.summaryItem}>
                  <Star size={20} color="#F59E0B" />
                  <Text style={styles.summaryNumber}>
                    {dailyReports.reduce((acc, r) => acc + r.completedLessons, 0)}
                  </Text>
                  <Text style={styles.summaryLabel}>Total Kelulusan</Text>
                </View>
              </View>
            </View>

            {/* Daily Reports List */}
            <View style={styles.studentsSection}>
              <Text style={styles.sectionTitle}>Laporan Per Hari</Text>
              {dailyReports.map((report) => (
                <DailyReportCard key={report.date} report={report} />
              ))}
              
              {dailyReports.length === 0 && (
                <View style={styles.emptyState}>
                  <Calendar size={48} color="#CBD5E1" />
                  <Text style={styles.emptyText}>Belum ada laporan harian</Text>
                </View>
              )}
            </View>
          </>
        )}
      </ScrollView>

      {/* Individual Detail Modal */}
      <Modal
        visible={showDetailModal}
        animationType="slide"
        presentationStyle="pageSheet"
      >
        <SafeAreaView style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <TouchableOpacity onPress={() => setShowDetailModal(false)}>
              <Text style={styles.closeButton}>Tutup</Text>
            </TouchableOpacity>
            <Text style={styles.modalTitle}>Detail Perkembangan</Text>
            <View style={{ width: 60 }} />
          </View>

          {selectedStudent && (
            <ScrollView style={styles.modalContent}>
              {/* Student Info */}
              <View style={styles.studentDetailHeader}>
                <View style={[
                  styles.avatarContainer,
                  { backgroundColor: getProgressColor(selectedStudent.attendanceRate) }
                ]}>
                  <User size={32} color="#FFFFFF" />
                </View>
                <View style={styles.studentDetailInfo}>
                  <Text style={styles.studentDetailName}>{selectedStudent.student.name}</Text>
                  <Text style={styles.studentDetailLevel}>{selectedStudent.student.readingLevel}</Text>
                  <Text style={styles.studentDetailPosition}>{selectedStudent.student.currentPosition}</Text>
                </View>
              </View>

              {/* Key Metrics */}
              <View style={styles.metricsContainer}>
                <View style={styles.metricCard}>
                  <Text style={styles.metricValue}>{selectedStudent.attendanceRate.toFixed(1)}%</Text>
                  <Text style={styles.metricLabel}>Tingkat Kehadiran</Text>
                  <View style={[styles.metricIndicator, { backgroundColor: getProgressColor(selectedStudent.attendanceRate) }]} />
                </View>
                <View style={styles.metricCard}>
                  <Text style={styles.metricValue}>{selectedStudent.currentStreak}</Text>
                  <Text style={styles.metricLabel}>Streak Saat Ini</Text>
                  <View style={[styles.metricIndicator, { backgroundColor: '#3B82F6' }]} />
                </View>
                <View style={styles.metricCard}>
                  <Text style={styles.metricValue}>{selectedStudent.completedLessons}</Text>
                  <Text style={styles.metricLabel}>Total Lulus</Text>
                  <View style={[styles.metricIndicator, { backgroundColor: '#22C55E' }]} />
                </View>
                <View style={styles.metricCard}>
                  <Text style={styles.metricValue}>{selectedStudent.failedLessons}</Text>
                  <Text style={styles.metricLabel}>Total Mengulang</Text>
                  <View style={[styles.metricIndicator, { backgroundColor: '#EF4444' }]} />
                </View>
              </View>

              {/* Attendance Stats */}
              <View style={{ flexDirection: 'row', justifyContent: 'center', marginBottom: 16, gap: 24 }}>
                <View style={{ alignItems: 'center' }}>
                  <Text style={{ fontSize: 18, fontWeight: 'bold', color: '#22C55E' }}>{selectedStudent.presentDays}</Text>
                  <Text style={{ color: '#22C55E' }}>Hadir</Text>
                </View>
                <View style={{ alignItems: 'center' }}>
                  <Text style={{ fontSize: 18, fontWeight: 'bold', color: '#EF4444' }}>{selectedStudent.totalDays - selectedStudent.presentDays}</Text>
                  <Text style={{ color: '#EF4444' }}>Tidak Hadir</Text>
                </View>
              </View>

              {/* Reading Progress */}
              <View style={styles.progressSection}>
                <Text style={styles.sectionTitle}>Perkembangan Bacaan</Text>
                <View style={styles.progressCard}>
                  <View style={styles.progressItem}>
                    <Text style={styles.progressLabel}>Posisi Awal (30 hari lalu)</Text>
                    <Text style={styles.progressValue}>{selectedStudent.initialPosition}</Text>
                  </View>
                  <View style={styles.progressItem}>
                    <Text style={styles.progressLabel}>Posisi Saat Ini</Text>
                    <Text style={styles.progressValue}>{selectedStudent.currentPosition}</Text>
                  </View>
                </View>
              </View>

              {/* Recent Notes */}
              {selectedStudent.recentNotes.length > 0 && (
                <View style={styles.notesSection}>
                  <Text style={styles.sectionTitle}>Catatan Guru Terbaru</Text>
                  {selectedStudent.recentNotes.map((note, index) => (
                    <View key={index} style={styles.noteCard}>
                      <Text style={styles.noteText}>{note}</Text>
                    </View>
                  ))}
                </View>
              )}

              {/* Recommendations */}
              <View style={styles.recommendationsSection}>
                <Text style={styles.sectionTitle}>Rekomendasi</Text>
                <View style={styles.recommendationCard}>
                  <Text style={styles.recommendationTitle}>
                    {selectedStudent.attendanceRate >= 80 ? 'Pertahankan Performa' : 'Tingkatkan Kehadiran'}
                  </Text>
                  <Text style={styles.recommendationText}>
                    {selectedStudent.attendanceRate >= 80 
                      ? `${selectedStudent.student.name} menunjukkan konsistensi yang baik. Terus berikan motivasi dan tantangan yang sesuai.`
                      : `${selectedStudent.student.name} perlu meningkatkan kehadiran. Pertimbangkan untuk memberikan perhatian khusus dan motivasi tambahan.`
                    }
                  </Text>
                </View>
                
                {selectedStudent.currentStreak >= 7 && (
                  <View style={styles.recommendationCard}>
                    <Text style={styles.recommendationTitle}>Apresiasi Konsistensi</Text>
                    <Text style={styles.recommendationText}>
                      Berikan apresiasi untuk streak kehadiran {selectedStudent.currentStreak} hari berturut-turut!
                    </Text>
                  </View>
                )}
              </View>

              {/* Action Buttons */}
              <View style={styles.actionButtons}>
                <TouchableOpacity 
                  style={[styles.actionButton, styles.shareButton]}
                  onPress={() => handleShareReport(selectedStudent)}
                >
                  <ShareIcon size={20} color="#FFFFFF" />
                  <Text style={styles.actionButtonText}>Bagikan Laporan</Text>
                </TouchableOpacity>
                
                <TouchableOpacity 
                  style={[styles.actionButton, styles.downloadButton]}
                  onPress={() => handleDownloadReport(selectedStudent)}
                >
                  <Download size={20} color="#FFFFFF" />
                  <Text style={styles.actionButtonText}>Unduh Laporan</Text>
                </TouchableOpacity>
              </View>
            </ScrollView>
          )}
        </SafeAreaView>
      </Modal>

      {/* Daily Report Detail Modal */}
      <Modal
        visible={showDailyModal}
        animationType="slide"
        presentationStyle="pageSheet"
      >
        <SafeAreaView style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <TouchableOpacity onPress={() => setShowDailyModal(false)}>
              <Text style={styles.closeButton}>Tutup</Text>
            </TouchableOpacity>
            <Text style={styles.modalTitle}>Laporan TPQ</Text>
            <View style={{ width: 60 }} />
          </View>

          {selectedDailyReport && (
            <ScrollView style={styles.modalContent}>
              {/* Date Header */}
              <View style={styles.dailyDetailHeader}>
                <Calendar size={24} color="#3B82F6" />
                <Text style={styles.dailyDetailDate}>{formatDate(selectedDailyReport.date)}</Text>
              </View>

              {/* Summary Stats */}
              <View style={styles.dailyDetailStats}>
                <View style={styles.dailyDetailStatCard}>
                  <Users size={20} color="#3B82F6" />
                  <Text style={styles.dailyDetailStatValue}>{selectedDailyReport.totalStudents}</Text>
                  <Text style={styles.dailyDetailStatLabel}>Total Siswa</Text>
                </View>
                <View style={styles.dailyDetailStatCard}>
                  <CheckCircle size={20} color="#22C55E" />
                  <Text style={styles.dailyDetailStatValue}>{selectedDailyReport.presentStudents}</Text>
                  <Text style={styles.dailyDetailStatLabel}>Hadir</Text>
                </View>
                <View style={styles.dailyDetailStatCard}>
                  <XCircle size={20} color="#EF4444" />
                  <Text style={styles.dailyDetailStatValue}>{selectedDailyReport.absentStudents}</Text>
                  <Text style={styles.dailyDetailStatLabel}>Tidak Hadir</Text>
                </View>
                <View style={styles.dailyDetailStatCard}>
                  <Star size={20} color="#F59E0B" />
                  <Text style={styles.dailyDetailStatValue}>{selectedDailyReport.completedLessons}</Text>
                  <Text style={styles.dailyDetailStatLabel}>Lulus</Text>
                </View>
              </View>

              {/* Student Details */}
              <View style={styles.studentDetailsSection}>
                <Text style={styles.sectionTitle}>Detail Kehadiran Siswa</Text>
                {selectedDailyReport.attendanceDetails.map(({ student, attendance }) => (
                  <View key={student.id} style={styles.studentDetailCard}>
                    <View style={styles.studentDetailRow}>
                      <View style={[
                        styles.studentStatusIndicator,
                        { backgroundColor: attendance?.present ? '#22C55E' : '#EF4444' }
                      ]}>
                        <User size={16} color="#FFFFFF" />
                      </View>
                      <View style={styles.studentDetailContent}>
                        <Text style={styles.studentDetailName}>{student.name}</Text>
                        <Text style={styles.studentDetailLevel}>{student.readingLevel}</Text>
                        {attendance && attendance.present && (
                          <Text style={styles.studentDetailReading}>
                            {attendance.readingType}: {attendance.currentReading}
                            {attendance.lessonCompleted && ' ✅ Lulus'}
                          </Text>
                        )}
                        {attendance && attendance.teacherNotes && (
                          <Text style={styles.studentDetailNotes}>"{attendance.teacherNotes}"</Text>
                        )}
                      </View>
                      <View style={styles.studentDetailStatus}>
                        <Text style={[
                          styles.studentDetailStatusText,
                          { color: attendance?.present ? '#22C55E' : '#EF4444' }
                        ]}>
                          {attendance?.present ? 'Hadir' : 'Tidak Hadir'}
                        </Text>
                      </View>
                    </View>
                  </View>
                ))}
              </View>
            </ScrollView>
          )}
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
  scrollView: {
    flex: 1,
  },
  reportTypeSelector: {
    flexDirection: 'row',
    marginHorizontal: 20,
    marginTop: 16,
    marginBottom: 8,
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 4,
  },
  reportTypeOption: {
    flex: 1,
    paddingVertical: 8,
    alignItems: 'center',
    borderRadius: 8,
  },
  reportTypeOptionSelected: {
    backgroundColor: '#3B82F6',
  },
  reportTypeOptionText: {
    fontSize: 14,
    color: '#64748B',
    fontWeight: '600',
  },
  reportTypeOptionTextSelected: {
    color: '#FFFFFF',
  },
  summaryContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 12,
  },
  summaryStats: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    paddingVertical: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  summaryItem: {
    alignItems: 'center',
    gap: 4,
  },
  summaryNumber: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  summaryLabel: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
  },
  studentsSection: {
    paddingHorizontal: 20,
    marginBottom: 20,
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
  progressStats: {
    alignItems: 'flex-end',
  },
  attendanceRate: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 2,
  },
  attendanceLabel: {
    fontSize: 12,
    color: '#64748B',
    marginBottom: 4,
  },
  quickStats: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingHorizontal: 16,
    paddingBottom: 12,
  },
  statItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  statText: {
    fontSize: 12,
    color: '#64748B',
  },
  progressBarContainer: {
    paddingHorizontal: 16,
    paddingBottom: 16,
  },
  progressBarBackground: {
    height: 4,
    backgroundColor: '#E2E8F0',
    borderRadius: 2,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    borderRadius: 2,
  },
  dailyCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    marginBottom: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  dailyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  dateContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  dateText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  dailyStats: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  dailyStatItem: {
    alignItems: 'center',
    gap: 4,
  },
  dailyStatText: {
    fontSize: 12,
    color: '#64748B',
  },
  attendanceRateText: {
    fontSize: 14,
    color: '#3B82F6',
    fontWeight: '600',
    textAlign: 'center',
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
  closeButton: {
    color: '#64748B',
    fontSize: 16,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  modalContent: {
    flex: 1,
    padding: 20,
  },
  studentDetailHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 20,
    marginBottom: 20,
    gap: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  studentDetailInfo: {
    flex: 1,
  },
  studentDetailName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
  },
  studentDetailLevel: {
    fontSize: 16,
    color: '#3B82F6',
    marginBottom: 4,
  },
  studentDetailPosition: {
    fontSize: 14,
    color: '#64748B',
  },
  metricsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 20,
  },
  metricCard: {
    flex: 1,
    minWidth: '45%',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  metricValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
  },
  metricLabel: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
    marginBottom: 8,
  },
  metricIndicator: {
    width: 30,
    height: 4,
    borderRadius: 2,
  },
  progressSection: {
    marginBottom: 20,
  },
  progressCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  progressItem: {
    marginBottom: 12,
  },
  progressLabel: {
    fontSize: 14,
    color: '#64748B',
    marginBottom: 4,
  },
  progressValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  notesSection: {
    marginBottom: 20,
  },
  noteCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 8,
    borderLeftWidth: 4,
    borderLeftColor: '#3B82F6',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  noteText: {
    fontSize: 14,
    color: '#475569',
    lineHeight: 20,
  },
  recommendationsSection: {
    marginBottom: 20,
  },
  recommendationCard: {
    backgroundColor: '#F0FDF4',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#BBF7D0',
  },
  recommendationTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#166534',
    marginBottom: 8,
  },
  recommendationText: {
    fontSize: 14,
    color: '#15803D',
    lineHeight: 20,
  },
  actionButtons: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 20,
  },
  actionButton: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    borderRadius: 12,
    gap: 8,
  },
  shareButton: {
    backgroundColor: '#3B82F6',
  },
  downloadButton: {
    backgroundColor: '#22C55E',
  },
  actionButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 14,
  },
  dailyDetailHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 20,
    marginBottom: 20,
    gap: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  dailyDetailDate: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
  },
  dailyDetailStats: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 20,
  },
  dailyDetailStatCard: {
    flex: 1,
    minWidth: '45%',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  dailyDetailStatValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1E293B',
    marginTop: 8,
    marginBottom: 4,
  },
  dailyDetailStatLabel: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
  },
  studentDetailsSection: {
    marginBottom: 20,
  },
  studentDetailCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  studentDetailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  studentStatusIndicator: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  studentDetailContent: {
    flex: 1,
  },
  studentDetailReading: {
    fontSize: 12,
    color: '#22C55E',
    marginTop: 2,
  },
  studentDetailNotes: {
    fontSize: 12,
    color: '#8B5CF6',
    fontStyle: 'italic',
    marginTop: 2,
  },
  studentDetailStatus: {
    alignItems: 'flex-end',
  },
  studentDetailStatusText: {
    fontSize: 12,
    fontWeight: '600',
  },
});