import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Modal,
  TextInput,
  ScrollView,
  Alert,
} from 'react-native';
import { BookOpen, ChevronDown } from 'lucide-react-native';

interface ReadingPositionInputProps {
  readingLevel: string;
  value: string;
  onPositionChange: (position: string) => void;
  placeholder?: string;
}

const QURAN_SURAHS = [
  { no: 1, name: 'Al-Fatihah', ayat: 7 },
  { no: 2, name: 'Al-Baqarah', ayat: 286 },
  { no: 3, name: 'Ali-Imran', ayat: 200 },
  { no: 4, name: 'An-Nisa', ayat: 176 },
  { no: 5, name: 'Al-Maidah', ayat: 120 },
  { no: 6, name: 'Al-Anam', ayat: 165 },
  { no: 7, name: 'Al-Araf', ayat: 206 },
  { no: 8, name: 'Al-Anfal', ayat: 75 },
  { no: 9, name: 'At-Taubah', ayat: 129 },
  { no: 10, name: 'Yunus', ayat: 109 },
  { no: 11, name: 'Hud', ayat: 123 },
  { no: 12, name: 'Yusuf', ayat: 111 },
  { no: 13, name: 'Ar-Rad', ayat: 43 },
  { no: 14, name: 'Ibrahim', ayat: 52 },
  { no: 15, name: 'Al-Hijr', ayat: 99 },
  { no: 16, name: 'An-Nahl', ayat: 128 },
  { no: 17, name: 'Al-Isra', ayat: 111 },
  { no: 18, name: 'Al-Kahfi', ayat: 110 },
  { no: 19, name: 'Maryam', ayat: 98 },
  { no: 20, name: 'Ta Ha', ayat: 135 },
  { no: 21, name: 'Al-Anbiya', ayat: 112 },
  { no: 22, name: 'Al-Hajj', ayat: 78 },
  { no: 23, name: 'Al-Muminun', ayat: 118 },
  { no: 24, name: 'An-Nur', ayat: 64 },
  { no: 25, name: 'Al-Furqan', ayat: 77 },
  { no: 26, name: 'Asy-Syuara', ayat: 227 },
  { no: 27, name: 'An-Naml', ayat: 93 },
  { no: 28, name: 'Al-Qasas', ayat: 88 },
  { no: 29, name: 'Al-Ankabut', ayat: 69 },
  { no: 30, name: 'Ar-Ruum', ayat: 60 },
  { no: 31, name: 'Luqman', ayat: 34 },
  { no: 32, name: 'As-Sajdah', ayat: 30 },
  { no: 33, name: 'Al-Ahzab', ayat: 73 },
  { no: 34, name: 'Saba', ayat: 54 },
  { no: 35, name: 'Fatir', ayat: 45 },
  { no: 36, name: 'Ya-Sin', ayat: 83 },
  { no: 37, name: 'Ash-Shaffat', ayat: 182 },
  { no: 38, name: 'Shad', ayat: 88 },
  { no: 39, name: 'Az-Zumar', ayat: 75 },
  { no: 40, name: 'Gafir', ayat: 85 },
  { no: 41, name: 'Fushshilat', ayat: 54 },
  { no: 42, name: 'Asy-Syura', ayat: 53 },
  { no: 43, name: 'Az-Zukhruf', ayat: 89 },
  { no: 44, name: 'Ad-Dukhan', ayat: 59 },
  { no: 45, name: 'Al-Jatsiyah', ayat: 37 },
  { no: 46, name: 'Al-Ahqaf', ayat: 35 },
  { no: 47, name: 'Muhammad', ayat: 38 },
  { no: 48, name: 'Al-Fath', ayat: 29 },
  { no: 49, name: 'Al-Hujurat', ayat: 18 },
  { no: 50, name: 'Qaf', ayat: 45 },
  { no: 51, name: 'Adz-Dzariyat', ayat: 60 },
  { no: 52, name: 'Ath-Thuur', ayat: 49 },
  { no: 53, name: 'An-Najm', ayat: 62 },
  { no: 54, name: 'Al-Qamar', ayat: 55 },
  { no: 55, name: 'Ar-Rahman', ayat: 78 },
  { no: 56, name: 'Al-Waqiah', ayat: 96 },
  { no: 57, name: 'Al-Hadid', ayat: 29 },
  { no: 58, name: 'Al-Mujadilah', ayat: 22 },
  { no: 59, name: 'Al-Hasyr', ayat: 24 },
  { no: 60, name: 'Al-Mumtahanah', ayat: 13 },
  { no: 61, name: 'Ash-Shaf', ayat: 14 },
  { no: 62, name: 'Al-Jumuah', ayat: 11 },
  { no: 63, name: 'Al-Munafiqun', ayat: 11 },
  { no: 64, name: 'At-Taghabun', ayat: 18 },
  { no: 65, name: 'Ath-Thalaq', ayat: 12 },
  { no: 66, name: 'At-Tahrim', ayat: 12 },
  { no: 67, name: 'Al-Mulk', ayat: 30 },
  { no: 68, name: 'Al-Qalam', ayat: 52 },
  { no: 69, name: 'Al-Haqqah', ayat: 52 },
  { no: 70, name: 'Al-Maarij', ayat: 44 },
  { no: 71, name: 'Nuh', ayat: 28 },
  { no: 72, name: 'Al-Jin', ayat: 28 },
  { no: 73, name: 'Al-Muzammil', ayat: 20 },
  { no: 74, name: 'Al-Muddatstsir', ayat: 56 },
  { no: 75, name: 'Al-Qiyamah', ayat: 40 },
  { no: 76, name: 'Al-Insan', ayat: 31 },
  { no: 77, name: 'Al-Mursalat', ayat: 50 },
  { no: 78, name: 'An-Naba', ayat: 40 },
  { no: 79, name: 'An-Naziat', ayat: 46 },
  { no: 80, name: 'Abasa', ayat: 42 },
  { no: 81, name: 'At-Takwir', ayat: 29 },
  { no: 82, name: 'Al-Infithar', ayat: 19 },
  { no: 83, name: 'Al-Muthaffifin', ayat: 36 },
  { no: 84, name: 'Al-Inshiqaq', ayat: 25 },
  { no: 85, name: 'Al-Buruj', ayat: 22 },
  { no: 86, name: 'Ath-Thariq', ayat: 17 },
  { no: 87, name: 'Al-Alaa', ayat: 19 },
  { no: 88, name: 'Al-Ghasyiyah', ayat: 26 },
  { no: 89, name: 'Al-Fajr', ayat: 30 },
  { no: 90, name: 'Al-Balad', ayat: 20 },
  { no: 91, name: 'Asy-Syams', ayat: 15 },
  { no: 92, name: 'Al-Lail', ayat: 21 },
  { no: 93, name: 'Adh-Dhuha', ayat: 11 },
  { no: 94, name: 'Al-Inshirah', ayat: 8 },
  { no: 95, name: 'At-Tin', ayat: 8 },
  { no: 96, name: 'Al-Alaq', ayat: 19 },
  { no: 97, name: 'Al-Qadr', ayat: 5 },
  { no: 98, name: 'Al-Bayyinah', ayat: 8 },
  { no: 99, name: 'Al-Zalzalah', ayat: 8 },
  { no: 100, name: 'Al-Adiyat', ayat: 11 },
  { no: 101, name: 'Al-Qariah', ayat: 11 },
  { no: 102, name: 'At-Takatsur', ayat: 8 },
  { no: 103, name: 'Al-Ashr', ayat: 3 },
  { no: 104, name: 'Al-Humazah', ayat: 9 },
  { no: 105, name: 'Al-Fil', ayat: 5 },
  { no: 106, name: 'Quraysh', ayat: 4 },
  { no: 107, name: 'Al-Maun', ayat: 7 },
  { no: 108, name: 'Al-Kautsar', ayat: 3 },
  { no: 109, name: 'Al-Kafirun', ayat: 6 },
  { no: 110, name: 'An-Nashr', ayat: 3 },
  { no: 111, name: 'Al-Lahab', ayat: 5 },
  { no: 112, name: 'Al-Ikhlas', ayat: 4 },
  { no: 113, name: 'Al-Falaq', ayat: 5 },
  { no: 114, name: 'An-Nas', ayat: 6 },
];

export default function ReadingPositionInput({ 
  readingLevel, 
  value, 
  onPositionChange, 
  placeholder = "Pilih posisi bacaan" 
}: ReadingPositionInputProps) {
  const [showModal, setShowModal] = useState(false);
  const [iqroPage, setIqroPage] = useState('1');
  const [selectedSurah, setSelectedSurah] = useState<typeof QURAN_SURAHS[0] | null>(null);
  const [ayatNumber, setAyatNumber] = useState('1');

  const isIqro = readingLevel.startsWith('Iqro');
  const iqroNumber = isIqro ? readingLevel.split(' ')[1] : '1';

  const openModal = () => {
    if (value && isIqro) {
      // Parse Iqro position: "Halaman 15"
      const pageMatch = value.match(/Halaman (\d+)/);
      if (pageMatch) {
        setIqroPage(pageMatch[1]);
      }
    } else if (value && !isIqro) {
      // Parse Al-Quran position: "Surat Al-Fatihah ayat 3"
      const surahMatch = value.match(/Surat (.+?) ayat (\d+)/);
      if (surahMatch) {
        const surahName = surahMatch[1];
        const ayat = surahMatch[2];
        const surah = QURAN_SURAHS.find(s => s.name === surahName);
        if (surah) {
          setSelectedSurah(surah);
          setAyatNumber(ayat);
        }
      }
    }
    setShowModal(true);
  };

  const handleConfirm = () => {
    if (isIqro) {
      const page = parseInt(iqroPage);
      if (isNaN(page) || page < 1) {
        Alert.alert('Error', 'Halaman harus berupa angka yang valid');
        return;
      }
      onPositionChange(`Halaman ${page}`);
    } else {
      if (!selectedSurah) {
        Alert.alert('Error', 'Pilih surat terlebih dahulu');
        return;
      }
      const ayat = parseInt(ayatNumber);
      if (isNaN(ayat) || ayat < 1 || ayat > selectedSurah.ayat) {
        Alert.alert('Error', `Ayat harus antara 1-${selectedSurah.ayat}`);
        return;
      }
      onPositionChange(`Surat ${selectedSurah.name} ayat ${ayat}`);
    }
    setShowModal(false);
  };

  const getDisplayValue = () => {
    if (!value) return '';
    if (isIqro) {
      return `${readingLevel} ${value}`;
    }
    return value;
  };

  return (
    <>
      <TouchableOpacity style={styles.positionInput} onPress={openModal}>
        <BookOpen size={20} color="#64748B" />
        <Text style={[styles.positionText, !value && styles.placeholderText]}>
          {getDisplayValue() || placeholder}
        </Text>
        <ChevronDown size={20} color="#64748B" />
      </TouchableOpacity>

      <Modal visible={showModal} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <TouchableOpacity onPress={() => setShowModal(false)}>
              <Text style={styles.cancelButton}>Batal</Text>
            </TouchableOpacity>
            <Text style={styles.modalTitle}>
              {isIqro ? `Posisi ${readingLevel}` : 'Posisi Al-Quran'}
            </Text>
            <TouchableOpacity onPress={handleConfirm}>
              <Text style={styles.confirmButton}>Pilih</Text>
            </TouchableOpacity>
          </View>

          <ScrollView style={styles.modalContent}>
            {isIqro ? (
              // Iqro Page Input
              <View style={styles.iqroContainer}>
                <Text style={styles.sectionTitle}>Halaman {readingLevel}</Text>
                <View style={styles.pageInputContainer}>
                  <Text style={styles.pageLabel}>Halaman:</Text>
                  <TextInput
                    style={styles.pageInput}
                    value={iqroPage}
                    onChangeText={setIqroPage}
                    keyboardType="numeric"
                    placeholder="1"
                  />
                </View>
                <Text style={styles.helpText}>
                  Masukkan nomor halaman terakhir yang dibaca
                </Text>
              </View>
            ) : (
              // Al-Quran Surah and Ayat Selection
              <View style={styles.quranContainer}>
                <Text style={styles.sectionTitle}>Pilih Surat</Text>
                <ScrollView style={styles.surahList}>
                  {QURAN_SURAHS.map((surah) => (
                    <TouchableOpacity
                      key={surah.no}
                      style={[
                        styles.surahItem,
                        selectedSurah?.no === surah.no && styles.selectedSurah
                      ]}
                      onPress={() => setSelectedSurah(surah)}
                    >
                      <View style={styles.surahInfo}>
                        <Text style={[
                          styles.surahNumber,
                          selectedSurah?.no === surah.no && styles.selectedSurahText
                        ]}>
                          {surah.no}
                        </Text>
                        <View style={styles.surahDetails}>
                          <Text style={[
                            styles.surahName,
                            selectedSurah?.no === surah.no && styles.selectedSurahText
                          ]}>
                            {surah.name}
                          </Text>
                          <Text style={[
                            styles.surahAyat,
                            selectedSurah?.no === surah.no && styles.selectedSurahText
                          ]}>
                            {surah.ayat} ayat
                          </Text>
                        </View>
                      </View>
                    </TouchableOpacity>
                  ))}
                </ScrollView>

                {selectedSurah && (
                  <View style={styles.ayatContainer}>
                    <Text style={styles.sectionTitle}>
                      Ayat dalam Surat {selectedSurah.name}
                    </Text>
                    <View style={styles.ayatInputContainer}>
                      <Text style={styles.ayatLabel}>Ayat:</Text>
                      <TextInput
                        style={styles.ayatInput}
                        value={ayatNumber}
                        onChangeText={setAyatNumber}
                        keyboardType="numeric"
                        placeholder="1"
                      />
                      <Text style={styles.ayatRange}>/ {selectedSurah.ayat}</Text>
                    </View>
                    <Text style={styles.helpText}>
                      Masukkan nomor ayat terakhir yang dibaca (1-{selectedSurah.ayat})
                    </Text>
                  </View>
                )}
              </View>
            )}
          </ScrollView>
        </View>
      </Modal>
    </>
  );
}

const styles = StyleSheet.create({
  positionInput: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 12,
  },
  positionText: {
    flex: 1,
    fontSize: 16,
    color: '#1E293B',
  },
  placeholderText: {
    color: '#94A3B8',
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
  confirmButton: {
    color: '#22C55E',
    fontSize: 16,
    fontWeight: '600',
  },
  modalContent: {
    flex: 1,
    padding: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1E293B',
    marginBottom: 16,
  },
  iqroContainer: {
    flex: 1,
  },
  pageInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    gap: 12,
  },
  pageLabel: {
    fontSize: 16,
    color: '#1E293B',
    fontWeight: '600',
  },
  pageInput: {
    flex: 1,
    fontSize: 18,
    color: '#1E293B',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    textAlign: 'center',
  },
  helpText: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 20,
  },
  quranContainer: {
    flex: 1,
  },
  surahList: {
    maxHeight: 300,
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    marginBottom: 20,
  },
  surahItem: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  selectedSurah: {
    backgroundColor: '#F0FDF4',
  },
  surahInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  surahNumber: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#64748B',
    width: 30,
    textAlign: 'center',
  },
  surahDetails: {
    flex: 1,
  },
  surahName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1E293B',
    marginBottom: 2,
  },
  surahAyat: {
    fontSize: 12,
    color: '#64748B',
  },
  selectedSurahText: {
    color: '#22C55E',
  },
  ayatContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
  },
  ayatInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
    gap: 12,
  },
  ayatLabel: {
    fontSize: 16,
    color: '#1E293B',
    fontWeight: '600',
  },
  ayatInput: {
    fontSize: 18,
    color: '#1E293B',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    textAlign: 'center',
    minWidth: 60,
  },
  ayatRange: {
    fontSize: 16,
    color: '#64748B',
  },
});