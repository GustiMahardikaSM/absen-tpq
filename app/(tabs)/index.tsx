import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  RefreshControl,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Users, CircleCheck as CheckCircle, Circle as XCircle, TrendingUp, BookOpen } from 'lucide-react-native';
import { studentService } from '@/services/studentService';
import { attendanceService } from '@/services/attendanceService';

interface DashboardStats {
  totalStudents: number;
  presentToday: number;
  absentToday: number;
  completedLessons: number;
  iqroStudents: number;
  quranStudents: number;
}

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats>({
    totalStudents: 0,
    presentToday: 0,
    absentToday: 0,
    completedLessons: 0,
    iqroStudents: 0,
    quranStudents: 0,
  });
  const [refreshing, setRefreshing] = useState(false);

  const loadDashboardData = async () => {
    try {
      const students = await studentService.getAllStudents();
      const today = new Date().toISOString().split('T')[0];
      const todayAttendance = await attendanceService.getAttendanceByDate(today);
      
      const presentCount = todayAttendance.filter(a => a.present).length;
      const completedCount = todayAttendance.filter(a => a.lessonCompleted).length;
      const iqroCount = students.filter(s => s.readingLevel.startsWith('Iqro')).length;
      const quranCount = students.filter(s => s.readingLevel === 'Al-Quran').length;

      setStats({
        totalStudents: students.length,
        presentToday: presentCount,
        absentToday: students.length - presentCount,
        completedLessons: completedCount,
        iqroStudents: iqroCount,
        quranStudents: quranCount,
      });
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadDashboardData();
    setRefreshing(false);
  };

  useEffect(() => {
    loadDashboardData();
  }, []);

  const StatCard = ({ 
    icon, 
    title, 
    value, 
    color, 
    backgroundColor 
  }: {
    icon: React.ReactNode;
    title: string;
    value: number;
    color: string;
    backgroundColor: string;
  }) => (
    <View style={[styles.statCard, { backgroundColor }]}>
      <View style={[styles.statIcon, { backgroundColor: color + '20' }]}>
        {icon}
      </View>
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statTitle}>{title}</Text>
    </View>
  );

  const today = new Date();
  const todayString = today.toLocaleDateString('id-ID', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView 
        style={styles.scrollView}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.greeting}>Assalamu'alaikum</Text>
          <Text style={styles.subtitle}>TPQ Attendance System</Text>
          <Text style={styles.date}>{todayString}</Text>
        </View>

        {/* Stats Grid */}
        <View style={styles.statsGrid}>
          <StatCard
            icon={<Users size={24} color="#3B82F6" />}
            title="Total Siswa"
            value={stats.totalStudents}
            color="#3B82F6"
            backgroundColor="#FFFFFF"
          />
          <StatCard
            icon={<CheckCircle size={24} color="#22C55E" />}
            title="Hadir Hari Ini"
            value={stats.presentToday}
            color="#22C55E"
            backgroundColor="#FFFFFF"
          />
          <StatCard
            icon={<XCircle size={24} color="#EF4444" />}
            title="Tidak Hadir"
            value={stats.absentToday}
            color="#EF4444"
            backgroundColor="#FFFFFF"
          />
          <StatCard
            icon={<TrendingUp size={24} color="#F59E0B" />}
            title="Lulus Hari Ini"
            value={stats.completedLessons}
            color="#F59E0B"
            backgroundColor="#FFFFFF"
          />
        </View>

        {/* Reading Progress */}
        <View style={styles.progressSection}>
          <Text style={styles.sectionTitle}>Sebaran Tingkat Bacaan</Text>
          <View style={styles.progressCards}>
            <View style={styles.progressCard}>
              <BookOpen size={20} color="#8B5CF6" />
              <Text style={styles.progressValue}>{stats.iqroStudents}</Text>
              <Text style={styles.progressLabel}>Siswa Iqro</Text>
            </View>
            <View style={styles.progressCard}>
              <BookOpen size={20} color="#22C55E" />
              <Text style={styles.progressValue}>{stats.quranStudents}</Text>
              <Text style={styles.progressLabel}>Siswa Al-Qur'an</Text>
            </View>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  scrollView: {
    flex: 1,
  },
  header: {
    padding: 20,
    backgroundColor: '#22C55E',
    marginBottom: 20,
  },
  greeting: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 16,
    color: '#FFFFFF',
    opacity: 0.9,
  },
  date: {
    fontSize: 14,
    color: '#FFFFFF',
    opacity: 0.8,
    marginTop: 8,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 20,
    gap: 12,
    marginBottom: 20,
  },
  statCard: {
    flex: 1,
    minWidth: '45%',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statIcon: {
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  statValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
  },
  statTitle: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
  },
  progressSection: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 12,
  },
  progressCards: {
    flexDirection: 'row',
    gap: 12,
  },
  progressCard: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  progressValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1E293B',
    marginTop: 8,
    marginBottom: 4,
  },
  progressLabel: {
    fontSize: 12,
    color: '#64748B',
    textAlign: 'center',
  },
});