# Tema Islami untuk Aplikasi TPQ

## Overview
Tema Islami ini dirancang khusus untuk aplikasi TPQ (Taman Pendidikan Al-Quran) dengan nuansa yang elegan, modern, dan sesuai dengan nilai-nilai Islam. Tema ini menggunakan kombinasi warna hijau dan emas yang mencerminkan identitas Islam, dengan desain yang modern dan user-friendly.

## Palet Warna

### Warna Utama
- **IslamicGreen** (`#1B5E20`) - Hijau tua sebagai warna primer
- **IslamicGreenLight** (`#2E7D32`) - Hijau muda untuk gradien
- **SoftGold** (`#FFD54F`) - Emas lembut untuk aksen
- **GoldAccent** (`#FFB300`) - Emas untuk highlight

### Warna Background
- **IvoryWhite** (`#FAFAFA`) - Putih gading untuk background utama
- **CreamWhite** (`#F5F5F5`) - Krem untuk background sekunder
- **DarkGreen** (`#0D4B1A`) - Hijau gelap untuk teks
- **LightGreen** (`#4CAF50`) - Hijau terang untuk elemen aktif

### Warna Gender
- **BoyBlue** (`#D0E8FF`) - Biru muda untuk siswa laki-laki
- **GirlPink** (`#FFD6E0`) - Pink muda untuk siswa perempuan

## Komponen UI Islami

### 1. IslamicTopAppBar
```kotlin
IslamicTopAppBar(
    title = "Daftar Santri TPQ",
    onNavigateBack = { /* action */ }
)
```
- TopAppBar dengan gradien hijau
- Teks putih dengan font bold
- Tombol back otomatis jika `onNavigateBack` disediakan

### 2. IslamicFloatingActionButton
```kotlin
IslamicFloatingActionButton(
    onClick = { /* action */ },
    icon = { Icon(Icons.Default.Add, "Tambah") }
)
```
- FAB dengan warna emas
- Bentuk lingkaran sempurna
- Icon kustom

### 3. IslamicSearchBar
```kotlin
IslamicSearchBar(
    value = searchQuery,
    onValueChange = { searchQuery = it },
    placeholder = "Cari Nama Santri..."
)
```
- Search bar dengan border hijau
- Icon search dengan warna hijau
- Placeholder text yang informatif

### 4. IslamicCard
```kotlin
IslamicCard {
    // Content
}
```
- Card dengan elevation dan border radius
- Background putih
- Shadow yang elegan

### 5. IslamicButton
```kotlin
IslamicButton(
    onClick = { /* action */ }
) {
    Text("Simpan")
}
```
- Button dengan warna hijau
- Border radius yang konsisten
- Teks putih

### 6. IslamicOutlinedButton
```kotlin
IslamicOutlinedButton(
    onClick = { /* action */ }
) {
    Text("Batal")
}
```
- Button outline dengan border hijau
- Teks hijau
- Gradien border

### 7. IslamicEmptyState
```kotlin
IslamicEmptyState(
    icon = "📖",
    title = "Belum ada santri terdaftar",
    subtitle = "Mulai dengan menambahkan santri baru"
)
```
- Empty state dengan ikon
- Teks yang informatif
- Desain yang menarik

### 8. IslamicLoadingIndicator
```kotlin
IslamicLoadingIndicator()
```
- Loading spinner dengan warna hijau
- Teks "Memuat..."
- Posisi center

### 9. IslamicDivider
```kotlin
IslamicDivider()
```
- Divider dengan warna hijau transparan
- Ketebalan 1dp

### 10. IslamicBadge
```kotlin
IslamicBadge(
    text = "Baru",
    color = SoftGold
)
```
- Badge dengan warna kustom
- Border radius yang konsisten
- Teks dengan font weight medium

## Implementasi di StudentListScreen

### Fitur Utama
1. **TopAppBar Islami** - Header dengan gradien hijau
2. **Search Bar** - Pencarian dengan tema hijau
3. **Floating Action Button** - Tombol tambah dengan warna emas
4. **Student Cards** - Card dengan gradien berdasarkan gender
5. **Empty State** - Tampilan kosong yang menarik
6. **Dialog Konfirmasi** - Dialog hapus dengan tema Islami

### Desain Card Siswa
- **Background Gradien**: Biru untuk laki-laki, pink untuk perempuan
- **Ornamen**: Simbol ☪ di pojok kanan atas
- **Informasi**: Nama, ID, bacaan, dan umur
- **Aksi**: Tombol edit dan hapus dengan background transparan
- **Elevation**: Shadow yang memberikan kesan floating

## Penggunaan

### 1. Import Tema
```kotlin
import com.example.myapplication.ui.theme.*
```

### 2. Gunakan Komponen
```kotlin
@Composable
fun MyScreen() {
    IslamicTopAppBar("Judul")
    IslamicSearchBar(value, onValueChange)
    IslamicFloatingActionButton(onClick = {})
}
```

### 3. Customisasi
Semua komponen dapat dikustomisasi dengan parameter yang disediakan:
- Warna
- Ukuran
- Icon
- Teks
- Callback functions

## Keunggulan Tema

1. **Konsistensi Visual** - Semua komponen menggunakan palet warna yang sama
2. **Aksesibilitas** - Kontras warna yang baik untuk readability
3. **Modern** - Menggunakan Material Design 3
4. **Responsif** - Beradaptasi dengan berbagai ukuran layar
5. **Islami** - Nuansa yang sesuai dengan nilai-nilai Islam
6. **Reusable** - Komponen dapat digunakan di seluruh aplikasi

## Tips Penggunaan

1. **Gunakan Gradien** - Untuk memberikan kesan modern dan elegan
2. **Konsisten dengan Spacing** - Gunakan padding dan margin yang konsisten
3. **Pilih Icon yang Tepat** - Gunakan emoji atau icon yang sesuai konteks
4. **Perhatikan Kontras** - Pastikan teks mudah dibaca
5. **Gunakan Elevation** - Untuk memberikan hierarki visual

## File yang Dimodifikasi

1. `Color.kt` - Penambahan palet warna Islami
2. `Theme.kt` - Konfigurasi tema dengan warna Islami
3. `Type.kt` - Typography yang elegan
4. `IslamicComponents.kt` - Komponen UI Islami
5. `StudentListScreen.kt` - Implementasi tema di daftar siswa

Tema ini memberikan pengalaman pengguna yang modern, elegan, dan sesuai dengan nilai-nilai Islam untuk aplikasi TPQ. 