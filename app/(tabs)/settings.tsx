import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  Linking,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Download, Upload, Database, Instagram, Linkedin } from 'lucide-react-native';
import { studentService } from '@/services/studentService';
import { attendanceService } from '@/services/attendanceService';
import * as DocumentPicker from 'expo-document-picker';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';

interface DatabaseExport {
  students: any[];
  attendance: any[];
  exportDate: string;
  version: string;
}

export default function Settings() {
  const [isExporting, setIsExporting] = useState(false);
  const [isImporting, setIsImporting] = useState(false);

  const handleExportDatabase = async () => {
    try {
      setIsExporting(true);
      
      // Get all data
      const students = await studentService.getAllStudents();
      const attendance = await attendanceService.getAllAttendance();
      
      const exportData: DatabaseExport = {
        students,
        attendance,
        exportDate: new Date().toISOString(),
        version: '1.0.0'
      };
      
      const jsonString = JSON.stringify(exportData, null, 2);
      const today = new Date();
      const dateString = `${today.getDate().toString().padStart(2, '0')}${(today.getMonth() + 1).toString().padStart(2, '0')}${today.getFullYear()}`;
      const fileName = `TPQ_Database_Export_${dateString}.json`;
      
      if (FileSystem.documentDirectory) {
        const fileUri = FileSystem.documentDirectory + fileName;
        await FileSystem.writeAsStringAsync(fileUri, jsonString);
        
        if (await Sharing.isAvailableAsync()) {
          await Sharing.shareAsync(fileUri, {
            mimeType: 'application/json',
            dialogTitle: 'Export Database TPQ'
          });
        } else {
          Alert.alert('Berhasil', `Database berhasil diekspor ke ${fileName}`);
        }
      } else {
        // Fallback for web
        if (typeof window !== 'undefined') {
          const blob = new Blob([jsonString], { type: 'application/json' });
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
          Alert.alert('Berhasil', 'Database berhasil diekspor');
        }
      }
    } catch (error) {
      console.error('Error exporting database:', error);
      Alert.alert('Error', 'Gagal mengekspor database');
    } finally {
      setIsExporting(false);
    }
  };

  const handleImportDatabase = async () => {
    try {
      setIsImporting(true);
      
      const result = await DocumentPicker.getDocumentAsync({
        type: 'application/json',
        copyToCacheDirectory: true
      });
      
      if (result.canceled) {
        setIsImporting(false);
        return;
      }
      
      let fileContent: string;
      
      // Platform-specific file reading
      if (Platform.OS === 'web') {
        // Use FileReader API for web
        const file = result.assets[0].file;
        if (!file) {
          Alert.alert('Error', 'File tidak dapat dibaca');
          setIsImporting(false);
          return;
        }
        
        fileContent = await new Promise<string>((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = (e) => {
            if (e.target?.result) {
              resolve(e.target.result as string);
            } else {
              reject(new Error('Failed to read file'));
            }
          };
          reader.onerror = () => reject(new Error('FileReader error'));
          reader.readAsText(file);
        });
      } else {
        // Use FileSystem for native platforms
        const fileUri = result.assets[0].uri;
        fileContent = await FileSystem.readAsStringAsync(fileUri);
      }
      
      const importData: DatabaseExport = JSON.parse(fileContent);
      
      if (!importData.students || !importData.attendance) {
        Alert.alert('Error', 'Format file tidak valid');
        setIsImporting(false);
        return;
      }
      
      Alert.alert(
        'Konfirmasi Import',
        `Akan mengimpor ${importData.students.length} siswa dan ${importData.attendance.length} data absensi.\n\n• Data dengan ID yang sama akan diperbarui\n• Data dengan ID baru akan ditambahkan\n\nLanjutkan?`,
        [
          { text: 'Batal', style: 'cancel' },
          {
            text: 'Import',
            onPress: async () => {
              try {
                // Import students using new method
                const studentResult = await studentService.importStudents(importData.students);
                
                // Import attendance using new method
                const attendanceResult = await attendanceService.importAttendance(importData.attendance);
                
                let message = `Import berhasil!\n\nSiswa:\n• ${studentResult.added} ditambahkan\n• ${studentResult.updated} diperbarui\n\nAbsensi:\n• ${attendanceResult.added} ditambahkan\n• ${attendanceResult.updated} diperbarui`;
                
                if (studentResult.errors.length > 0 || attendanceResult.errors.length > 0) {
                  message += `\n\nError:\n${[...studentResult.errors, ...attendanceResult.errors].join('\n')}`;
                }
                
                Alert.alert('Import Selesai', message);
              } catch (error) {
                console.error('Error importing database:', error);
                Alert.alert('Error', 'Gagal mengimpor database: ' + error);
              }
            }
          }
        ]
      );
    } catch (error) {
      console.error('Error importing database:', error);
      Alert.alert('Error', 'Gagal membaca file database: ' + error);
    } finally {
      setIsImporting(false);
    }
  };

  const openInstagram = () => {
    Linking.openURL('https://www.instagram.com/gusti_mahardika_sm/');
  };

  const openLinkedIn = () => {
    Linking.openURL('https://www.linkedin.com/in/gusti-mahardika-sm');
  };

  const SettingItem = ({ 
    icon, 
    title, 
    subtitle, 
    onPress,
    disabled = false
  }: {
    icon: React.ReactNode;
    title: string;
    subtitle?: string;
    onPress?: () => void;
    disabled?: boolean;
  }) => (
    <TouchableOpacity 
      style={[styles.settingItem, disabled && styles.settingItemDisabled]} 
      onPress={onPress}
      disabled={disabled}
    >
      <View style={styles.settingIcon}>
        {icon}
      </View>
      <View style={styles.settingContent}>
        <Text style={[styles.settingTitle, disabled && styles.settingTitleDisabled]}>{title}</Text>
        {subtitle && <Text style={[styles.settingSubtitle, disabled && styles.settingSubtitleDisabled]}>{subtitle}</Text>}
      </View>
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
        {/* Database Management */}
        <SettingSection title="Manajemen Database">
          <SettingItem
            icon={<Upload size={24} color="#22C55E" />}
            title="Ekspor Database"
            subtitle="Simpan seluruh data siswa dan absensi"
            onPress={handleExportDatabase}
            disabled={isExporting}
          />
          <SettingItem
            icon={<Download size={24} color="#3B82F6" />}
            title="Impor Database"
            subtitle="Muat data dari file backup (ID sama akan diperbarui, ID baru akan ditambahkan)"
            onPress={handleImportDatabase}
            disabled={isImporting}
          />
        </SettingSection>

        {/* Developer Info */}
        <View style={styles.developerSection}>
          <Text style={styles.developerTitle}>Dikembangkan oleh</Text>
          <Text style={styles.developerName}>Gusti Mahardika Surya Maulana</Text>
          <Text style={styles.developerInfo}>UNDIP Teknik Elektro 2022</Text>
          
          <View style={styles.socialLinks}>
            <TouchableOpacity style={styles.socialButton} onPress={openInstagram}>
              <Instagram size={20} color="#E4405F" />
              <Text style={styles.socialText}>@gusti_mahardika_sm</Text>
            </TouchableOpacity>
            
            <TouchableOpacity style={styles.socialButton} onPress={openLinkedIn}>
              <Linkedin size={20} color="#0077B5" />
              <Text style={styles.socialText}>gusti-mahardika-sm</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* App Info */}
        <View style={styles.appInfo}>
          <Text style={styles.appName}>TPQ Attendance System</Text>
          <Text style={styles.appVersion}>Versi 1.0.0</Text>
          <Text style={styles.appDescription}>
            Aplikasi manajemen absensi dan perkembangan siswa TPQ
          </Text>
          <Text style={styles.idFormatInfo}>
            Format ID: YYMMDDHHmmss (Tahun-Bulan-Tanggal-Jam-Menit-Detik)
          </Text>
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
  settingItemDisabled: {
    opacity: 0.5,
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
  settingTitleDisabled: {
    color: '#94A3B8',
  },
  settingSubtitle: {
    fontSize: 14,
    color: '#64748B',
  },
  settingSubtitleDisabled: {
    color: '#94A3B8',
  },
  developerSection: {
    backgroundColor: '#FFFFFF',
    marginHorizontal: 20,
    marginTop: 20,
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  developerTitle: {
    fontSize: 14,
    color: '#64748B',
    marginBottom: 8,
  },
  developerName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 4,
    textAlign: 'center',
  },
  developerInfo: {
    fontSize: 14,
    color: '#64748B',
    marginBottom: 16,
  },
  socialLinks: {
    gap: 12,
    width: '100%',
  },
  socialButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#F8FAFC',
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 8,
    gap: 8,
  },
  socialText: {
    fontSize: 14,
    color: '#1E293B',
    fontWeight: '500',
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
    marginBottom: 8,
  },
  idFormatInfo: {
    fontSize: 12,
    color: '#94A3B8',
    textAlign: 'center',
    fontStyle: 'italic',
  },
});