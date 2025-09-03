import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Switch,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { User, Download, Upload, Shield, Bell, Palette, CircleHelp as HelpCircle, LogOut, ChevronRight, Database, FileText } from 'lucide-react-native';

export default function Settings() {
  const [notifications, setNotifications] = useState(true);
  const [darkMode, setDarkMode] = useState(false);
  const [autoBackup, setAutoBackup] = useState(true);

  const handleBackupData = () => {
    Alert.alert(
      'Backup Data',
      'Data akan dibackup ke penyimpanan lokal. Lanjutkan?',
      [
        { text: 'Batal', style: 'cancel' },
        { 
          text: 'Backup', 
          onPress: () => {
            // Implement backup logic here
            Alert.alert('Berhasil', 'Data berhasil dibackup');
          }
        },
      ]
    );
  };

  const handleRestoreData = () => {
    Alert.alert(
      'Restore Data',
      'Apakah Anda yakin ingin mengembalikan data dari backup? Data saat ini akan digantikan.',
      [
        { text: 'Batal', style: 'cancel' },
        { 
          text: 'Restore', 
          style: 'destructive',
          onPress: () => {
            // Implement restore logic here
            Alert.alert('Berhasil', 'Data berhasil direstore');
          }
        },
      ]
    );
  };

  const handleExportData = () => {
    Alert.alert(
      'Export Data',
      'Data akan diekspor dalam format CSV. Lanjutkan?',
      [
        { text: 'Batal', style: 'cancel' },
        { 
          text: 'Export', 
          onPress: () => {
            // Implement export logic here
            Alert.alert('Berhasil', 'Data berhasil diekspor');
          }
        },
      ]
    );
  };

  const handleLogout = () => {
    Alert.alert(
      'Keluar',
      'Apakah Anda yakin ingin keluar dari aplikasi?',
      [
        { text: 'Batal', style: 'cancel' },
        { 
          text: 'Keluar', 
          style: 'destructive',
          onPress: () => {
            // Implement logout logic here
            Alert.alert('Berhasil', 'Anda telah keluar dari aplikasi');
          }
        },
      ]
    );
  };

  const SettingItem = ({ 
    icon, 
    title, 
    subtitle, 
    onPress, 
    rightComponent 
  }: {
    icon: React.ReactNode;
    title: string;
    subtitle?: string;
    onPress?: () => void;
    rightComponent?: React.ReactNode;
  }) => (
    <TouchableOpacity style={styles.settingItem} onPress={onPress}>
      <View style={styles.settingIcon}>
        {icon}
      </View>
      <View style={styles.settingContent}>
        <Text style={styles.settingTitle}>{title}</Text>
        {subtitle && <Text style={styles.settingSubtitle}>{subtitle}</Text>}
      </View>
      {rightComponent || <ChevronRight size={20} color="#CBD5E1" />}
    </TouchableOpacity>
  );

  const SettingSection = ({ title, children }: { title: string; children: React.ReactNode }) => (
    <View style={styles.settingSection}>
      <Text style={styles.sectionTitle}>{title}</Text>
      <View style={styles.sectionContent}>
        {children}
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Pengaturan</Text>
      </View>

      <ScrollView style={styles.scrollView}>
        {/* Profile Section */}
        <SettingSection title="Profil">
          <SettingItem
            icon={<User size={24} color="#3B82F6" />}
            title="Admin TPQ"
            subtitle="Ustadz/Ustadzah"
            onPress={() => Alert.alert('Info', 'Fitur profil akan segera tersedia')}
          />
        </SettingSection>

        {/* Data Management */}
        <SettingSection title="Manajemen Data">
          <SettingItem
            icon={<Download size={24} color="#22C55E" />}
            title="Backup Data"
            subtitle="Simpan data ke penyimpanan lokal"
            onPress={handleBackupData}
          />
          <SettingItem
            icon={<Upload size={24} color="#F59E0B" />}
            title="Restore Data"
            subtitle="Kembalikan data dari backup"
            onPress={handleRestoreData}
          />
          <SettingItem
            icon={<FileText size={24} color="#8B5CF6" />}
            title="Export Data"
            subtitle="Export ke file CSV/Excel"
            onPress={handleExportData}
          />
          <SettingItem
            icon={<Database size={24} color="#EC4899" />}
            title="Auto Backup"
            subtitle="Backup otomatis setiap hari"
            rightComponent={
              <Switch
                value={autoBackup}
                onValueChange={setAutoBackup}
                trackColor={{ false: '#E5E7EB', true: '#BBF7D0' }}
                thumbColor={autoBackup ? '#22C55E' : '#F3F4F6'}
              />
            }
          />
        </SettingSection>

        {/* App Settings */}
        <SettingSection title="Pengaturan Aplikasi">
          <SettingItem
            icon={<Bell size={24} color="#F59E0B" />}
            title="Notifikasi"
            subtitle="Pengingat kehadiran dan backup"
            rightComponent={
              <Switch
                value={notifications}
                onValueChange={setNotifications}
                trackColor={{ false: '#E5E7EB', true: '#BBF7D0' }}
                thumbColor={notifications ? '#22C55E' : '#F3F4F6'}
              />
            }
          />
          <SettingItem
            icon={<Palette size={24} color="#8B5CF6" />}
            title="Mode Gelap"
            subtitle="Tampilan tema gelap"
            rightComponent={
              <Switch
                value={darkMode}
                onValueChange={setDarkMode}
                trackColor={{ false: '#E5E7EB', true: '#BBF7D0' }}
                thumbColor={darkMode ? '#22C55E' : '#F3F4F6'}
              />
            }
          />
        </SettingSection>

        {/* Security */}
        <SettingSection title="Keamanan">
          <SettingItem
            icon={<Shield size={24} color="#EF4444" />}
            title="Keamanan Data"
            subtitle="Proteksi dengan PIN/Password"
            onPress={() => Alert.alert('Info', 'Fitur keamanan akan segera tersedia')}
          />
        </SettingSection>

        {/* Help & Support */}
        <SettingSection title="Bantuan & Dukungan">
          <SettingItem
            icon={<HelpCircle size={24} color="#3B82F6" />}
            title="Panduan Penggunaan"
            subtitle="Tutorial lengkap aplikasi"
            onPress={() => Alert.alert('Info', 'Fitur panduan akan segera tersedia')}
          />
        </SettingSection>

        {/* App Info */}
        <View style={styles.appInfo}>
          <Text style={styles.appName}>TPQ Attendance System</Text>
          <Text style={styles.appVersion}>Versi 1.0.0</Text>
          <Text style={styles.appDescription}>
            Aplikasi manajemen absensi dan perkembangan siswa TPQ
          </Text>
        </View>

        {/* Logout */}
        <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
          <LogOut size={20} color="#EF4444" />
          <Text style={styles.logoutText}>Keluar</Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  header: {
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
  settingSection: {
    marginTop: 20,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#64748B',
    marginLeft: 20,
    marginBottom: 8,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  sectionContent: {
    backgroundColor: '#FFFFFF',
    marginHorizontal: 20,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
    gap: 12,
  },
  settingIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#F8FAFC',
    alignItems: 'center',
    justifyContent: 'center',
  },
  settingContent: {
    flex: 1,
  },
  settingTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
    marginBottom: 2,
  },
  settingSubtitle: {
    fontSize: 14,
    color: '#64748B',
  },
  appInfo: {
    alignItems: 'center',
    paddingVertical: 24,
    paddingHorizontal: 20,
    marginTop: 20,
  },
  appName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
  },
  appVersion: {
    fontSize: 14,
    color: '#64748B',
    marginBottom: 8,
  },
  appDescription: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 20,
  },
  logoutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FEF2F2',
    marginHorizontal: 20,
    marginVertical: 20,
    paddingVertical: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#FECACA',
    gap: 8,
  },
  logoutText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#EF4444',
  },
});