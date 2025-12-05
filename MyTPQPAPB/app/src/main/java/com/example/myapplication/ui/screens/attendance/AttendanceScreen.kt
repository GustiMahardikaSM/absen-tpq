package com.example.myapplication.ui.screens.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

/**
 * AttendanceScreen - Halaman pencatatan kehadiran siswa TPQ
 * Menampilkan daftar siswa dengan status kehadiran dan fitur input detail bacaan
 * @param onNavigateBack Callback untuk kembali ke halaman sebelumnya
 * @param viewModel ViewModel untuk mengelola data kehadiran
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttendanceViewModel = viewModel()
) {
    MyApplicationTheme(darkTheme = false) {
        // State management untuk data kehadiran dan UI
        val attendanceState by viewModel.attendanceState.collectAsState()
        val attendanceStats by viewModel.attendanceStats.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.selectedDate
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )
        var showInputDialog by remember { mutableStateOf(false) }
        var selectedStudentCode by remember { mutableStateOf<String?>(null) }
        var modeIqro by remember { mutableStateOf(true) }
        var iqroNumber by remember { mutableStateOf(1) }
        var iqroPage by remember { mutableStateOf(1) }
        var quranSurah by remember { mutableStateOf(1) }
        var quranAyat by remember { mutableStateOf(1) }
        var isPassed by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }
        val filteredAttendanceState = if (searchQuery.isBlank()) attendanceState else attendanceState.filter { it.student.name.contains(searchQuery, ignoreCase = true) }
        var catatanGuru by remember { mutableStateOf("") }
        var showInputError by remember { mutableStateOf<String?>(null) }
        // List nama surah Al-Quran dengan nomor dan jumlah ayat
        val surahList = listOf(
            Triple(1, "Al-Fatihah", 7), Triple(2, "Al-Baqarah", 286), Triple(3, "Ali-Imran", 200), Triple(4, "An-Nisa", 176), Triple(5, "Al-Maidah", 120), Triple(6, "Al-Anam", 165), Triple(7, "Al-Araf", 206), Triple(8, "Al-Anfal", 75), Triple(9, "At-Taubah", 129), Triple(10, "Yunus", 109), Triple(11, "Hud", 123), Triple(12, "Yusuf", 111), Triple(13, "Ar-Rad", 43), Triple(14, "Ibrahim", 52), Triple(15, "Al-Hijr", 99), Triple(16, "An-Nahl", 128), Triple(17, "Al-Isra", 111), Triple(18, "Al-Kahfi", 110), Triple(19, "Maryam", 98), Triple(20, "Ta Ha", 135), Triple(21, "Al-Anbiya", 112), Triple(22, "Al-Hajj", 78), Triple(23, "Al-Muminun", 118), Triple(24, "An-Nur", 64), Triple(25, "Al-Furqan", 77), Triple(26, "Asy-Syuara", 227), Triple(27, "An-Naml", 93), Triple(28, "Al-Qasas", 88), Triple(29, "Al-Ankabut", 69), Triple(30, "Ar-Ruum", 60), Triple(31, "Luqman", 34), Triple(32, "As-Sajdah", 30), Triple(33, "Al-Ahzab", 73), Triple(34, "Saba", 54), Triple(35, "Fatir", 45), Triple(36, "Ya-Sin", 83), Triple(37, "Ash-Shaffat", 182), Triple(38, "Shad", 88), Triple(39, "Az-Zumar", 75), Triple(40, "Gafir", 85), Triple(41, "Fushshilat", 54), Triple(42, "Asy-Syura", 53), Triple(43, "Az-Zukhruf", 89), Triple(44, "Ad-Dukhan", 59), Triple(45, "Al-Jatsiyah", 37), Triple(46, "Al-Ahqaf", 35), Triple(47, "Muhammad", 38), Triple(48, "Al-Fath", 29), Triple(49, "Al-Hujurat", 18), Triple(50, "Qaf", 45), Triple(51, "Adz-Dzariyat", 60), Triple(52, "Ath-Thuur", 49), Triple(53, "An-Najm", 62), Triple(54, "Al-Qamar", 55), Triple(55, "Ar-Rahman", 78), Triple(56, "Al-Waqiah", 96), Triple(57, "Al-Hadid", 29), Triple(58, "Al-Mujadilah", 22), Triple(59, "Al-Hasyr", 24), Triple(60, "Al-Mumtahanah", 13), Triple(61, "Ash-Shaf", 14), Triple(62, "Al-Jumuah", 11), Triple(63, "Al-Munafiqun", 11), Triple(64, "At-Taghabun", 18), Triple(65, "Ath-Thalaq", 12), Triple(66, "At-Tahrim", 12), Triple(67, "Al-Mulk", 30), Triple(68, "Al-Qalam", 52), Triple(69, "Al-Haqqah", 52), Triple(70, "Al-Maarij", 44), Triple(71, "Nuh", 28), Triple(72, "Al-Jin", 28), Triple(73, "Al-Muzammil", 20), Triple(74, "Al-Muddatstsir", 56), Triple(75, "Al-Qiyamah", 40), Triple(76, "Al-Insan", 31), Triple(77, "Al-Mursalat", 50), Triple(78, "An-Naba", 40), Triple(79, "An-Naziat", 46), Triple(80, "Abasa", 42), Triple(81, "At-Takwir", 29), Triple(82, "Al-Infithar", 19), Triple(83, "Al-Muthaffifin", 36), Triple(84, "Al-Inshiqaq", 25), Triple(85, "Al-Buruj", 22), Triple(86, "Ath-Thariq", 17), Triple(87, "Al-Alaa", 19), Triple(88, "Al-Ghasyiyah", 26), Triple(89, "Al-Fajr", 30), Triple(90, "Al-Balad", 20), Triple(91, "Asy-Syams", 15), Triple(92, "Al-Lail", 21), Triple(93, "Adh-Dhuha", 11), Triple(94, "Al-Inshirah", 8), Triple(95, "At-Tin", 8), Triple(96, "Al-Alaq", 19), Triple(97, "Al-Qadr", 5), Triple(98, "Al-Bayyinah", 8), Triple(99, "Al-Zalzalah", 8), Triple(100, "Al-Adiyat", 11), Triple(101, "Al-Qariah", 11), Triple(102, "At-Takatsur", 8), Triple(103, "Al-Ashr", 3), Triple(104, "Al-Humazah", 9), Triple(105, "Al-Fil", 5), Triple(106, "Quraysh", 4), Triple(107, "Al-Maun", 7), Triple(108, "Al-Kautsar", 3), Triple(109, "Al-Kafirun", 6), Triple(110, "An-Nashr", 3), Triple(111, "Al-Lahab", 5), Triple(112, "Al-Ikhlas", 4), Triple(113, "Al-Falaq", 5), Triple(114, "An-Nas", 6)
        )
        val selectedSurah = surahList.find { it.first == quranSurah } ?: surahList[0]
        val ayatMax = selectedSurah.third

        // Scaffold dengan TopAppBar dan background Islami
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Absensi Santri",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = IslamicGreen, // Background hijau Islami
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = IvoryWhite // Background putih gading
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(IvoryWhite)
                    .padding(padding)
            ) {
                // Kartu tanggal & statistik kehadiran
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp), // Border radius yang lembut
                    elevation = CardDefaults.cardElevation(8.dp), // Shadow untuk efek floating
                    colors = CardDefaults.cardColors(containerColor = IvoryWhite) // Background putih gading
                ) {
                    Box {
                        // Ornamen motif di pojok kanan atas (dihapus atau bisa pakai Icons.Default.Star)
                        // Icon(
                        //     Icons.Default.Star,
                        //     contentDescription = null,
                        //     modifier = Modifier
                        //         .size(80.dp)
                        //         .align(Alignment.TopEnd)
                        //         .alpha(0.1f)
                        // )
                        Column(Modifier.padding(16.dp)) {
                            // Tanggal dengan tombol ganti tanggal
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = viewModel.selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IslamicGreen // Warna hijau untuk tanggal
                                )
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, tint = SoftGold, contentDescription = null) // Icon emas untuk tombol tanggal
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Statistik kehadiran (hadir, absen, total)
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Loop untuk menampilkan statistik (Total, Hadir, Absen)
                                listOf(
                                    "Total" to attendanceStats.totalStudents.toString(),
                                    "Hadir" to attendanceStats.presentCount.toString(),
                                    "Absen" to (attendanceStats.totalStudents - attendanceStats.presentCount).toString()
                                ).forEach { (label, value) ->
                                    // Card statistik untuk setiap kategori
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .border(
                                                    BorderStroke(2.dp, SoftGold), // Border emas untuk statistik
                                                    shape = CircleShape // Bentuk lingkaran
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(value, style = MaterialTheme.typography.titleLarge) // Angka statistik
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(label, style = MaterialTheme.typography.bodySmall) // Label (Total/Hadir/Absen)
                                    }
                                }
                            }
                        }
                    }
                }
                // Search field untuk mencari siswa
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, tint = IslamicGreen, contentDescription = null) }, // Icon hijau untuk search
                    placeholder = { Text("Cari Nama Santri", color = Color.Gray) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = IslamicGreen, // Border hijau saat fokus
                        unfocusedBorderColor = SoftGold, // Border emas saat tidak fokus
                        cursorColor = IslamicGreen, // Cursor hijau
                        focusedLabelColor = IslamicGreen // Label hijau saat fokus
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                // SwipeRefresh untuk pull-to-refresh
                val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = { viewModel.refresh() } // Refresh data kehadiran
                ) {
                    // Daftar absensi siswa
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing antar item
                    ) {
                        items(filteredAttendanceState) { studentAttendance ->
                            // Item kehadiran untuk setiap siswa
                            AttendanceItem(
                                studentAttendance = studentAttendance,
                                onToggleAttendance = {
                                    selectedStudentCode = studentAttendance.student.studentCode
                                    // Otomatis set mode dan field input sesuai data Student
                                    val s = studentAttendance.student
                                    modeIqro = s.iqroNumber != null || (s.quranSurah == null) // Set mode Iqro jika ada data Iqro
                                    iqroNumber = s.iqroNumber ?: 0 // Set nomor jilid Iqro
                                    iqroPage = s.iqroPage ?: 0 // Set halaman Iqro
                                    quranSurah = if (s.quranSurah == null || s.quranSurah == 0) 1 else s.quranSurah // Set surah Al-Quran
                                    quranAyat = if (s.quranAyat == null || s.quranAyat == 0) 1 else s.quranAyat // Set ayat Al-Quran
                                    if (modeIqro && s.iqroNumber == null) iqroNumber = 0 // Reset jika tidak ada data Iqro
                                    if (modeIqro && s.iqroPage == null) iqroPage = 0 // Reset jika tidak ada data Iqro
                                    if (!modeIqro && (s.quranSurah == null || s.quranSurah == 0)) quranSurah = 1 // Reset surah jika tidak ada data Quran
                                    if (!modeIqro && (s.quranAyat == null || s.quranAyat == 0)) quranAyat = 1 // Reset ayat jika tidak ada data Quran
                                    showInputDialog = true // Tampilkan dialog input detail bacaan
                                }
                            )
                        }
                    }
                }
            }
        }
        // Date picker untuk memilih tanggal absensi
        if (showDatePicker) {
            // Dialog untuk memilih tanggal
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            // Konversi millis ke LocalDate dan update ViewModel
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                                viewModel.onDateSelected(selectedDate) // Update tanggal di ViewModel
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Batal")
                    }
                }
            ) {
                // DatePicker component untuk memilih tanggal
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false, // Tidak menampilkan toggle mode
                    title = { Text("Pilih Tanggal") }
                )
            }
        }
        // Dialog input detail bacaan untuk siswa
        if (showInputDialog && selectedStudentCode != null) {
            val selectedStudent = attendanceState.find { it.student.studentCode == selectedStudentCode }
            // Dialog untuk input detail bacaan (Iqro/Quran) dan catatan guru
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = {
                    // Header dialog dengan background emas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftGold, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // Background emas dengan border radius
                    ) {
                        Text(
                            "Input Bacaan & Status",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White // Text putih untuk kontras dengan background emas
                        )
                    }
                },
                text = {
                    // Konten dialog untuk input detail bacaan
                    Column {
                        // Informasi nama siswa yang sedang diinput
                        selectedStudent?.let { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer // Background container
                                )
                            ) {
                                // Card untuk menampilkan info siswa
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "Nama Siswa:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer // Warna text untuk kontras
                                    )
                                    Text(
                                        text = student.student.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold // Nama siswa dengan style bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        
                        // Toggle mode Iqro/Quran untuk memilih jenis bacaan
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = modeIqro, onClick = { modeIqro = true })
                            Text("Iqro", modifier = Modifier.clickable { modeIqro = true })
                            Spacer(Modifier.width(16.dp))
                            RadioButton(selected = !modeIqro, onClick = {
                                modeIqro = false
                                if (quranSurah == 0 || quranSurah == null) quranSurah = 1 // Reset surah jika kosong
                                if (quranAyat == 0 || quranAyat == null) quranAyat = 1 // Reset ayat jika kosong
                            })
                            Text("Quran", modifier = Modifier.clickable {
                                modeIqro = false
                                if (quranSurah == 0 || quranSurah == null) quranSurah = 1 // Reset surah jika kosong
                                if (quranAyat == 0 || quranAyat == null) quranAyat = 1 // Reset ayat jika kosong
                            })
                        }
                        // Input untuk mode Iqro
                        if (modeIqro) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Iqro / Qiroati:")
                                    Spacer(Modifier.width(8.dp))
                                    // Dropdown Jilid Iqro/Qiroati
                                    var expandedJilid by remember { mutableStateOf(false) }
                                    val tingkatList = listOf("Pra-TK", "1", "2", "3", "4", "5", "6") // List tingkat Iqro
                                    // Dropdown untuk memilih jilid Iqro
                                    Box {
                                        Text(
                                            text = when (iqroNumber) {
                                                0 -> "Pra-TK" // Pra-TK untuk jilid 0
                                                1,2,3,4,5,6 -> tingkatList[iqroNumber] // Jilid 1-6
                                                else -> tingkatList[0]
                                            },
                                            modifier = Modifier
                                                .width(100.dp)
                                                .clickable { expandedJilid = true } // Klik untuk membuka dropdown
                                                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.shapes.small)
                                                .padding(8.dp)
                                        )
                                        // Dropdown menu untuk pilihan jilid Iqro
                                        DropdownMenu(
                                            expanded = expandedJilid,
                                            onDismissRequest = { expandedJilid = false }
                                        ) {
                                            // Loop untuk setiap pilihan jilid Iqro
                                            tingkatList.forEachIndexed { idx, label ->
                                                DropdownMenuItem(
                                                    text = { Text(label) },
                                                    onClick = {
                                                        iqroNumber = idx // Set nomor jilid yang dipilih
                                                        expandedJilid = false // Tutup dropdown
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                // Input halaman Iqro
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Halaman:")
                                    Spacer(Modifier.width(8.dp))
                                    TextField(
                                        value = if (iqroPage == 0) "" else iqroPage.toString(),
                                        onValueChange = { iqroPage = it.toIntOrNull() ?: 0 }, // Konversi ke integer atau default 0
                                        label = { Text("Hal") },
                                        modifier = Modifier.width(80.dp)
                                    )
                                }
                            }
                        // Input untuk mode Quran
                        } else {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Surah:")
                                    Spacer(Modifier.width(8.dp))
                                    // Dropdown Surah Al-Quran
                                    var expandedSurah by remember { mutableStateOf(false) }
                                    // Dropdown untuk memilih surah Al-Quran
                                    Box {
                                        Text(
                                            text = "${selectedSurah.first}. ${selectedSurah.second}", // Format: "1. Al-Fatihah"
                                            modifier = Modifier
                                                .width(160.dp)
                                                .clickable { expandedSurah = true } // Klik untuk membuka dropdown
                                                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.shapes.small)
                                                .padding(8.dp)
                                        )
                                        // Dropdown menu untuk pilihan surah Al-Quran
                                        DropdownMenu(
                                            expanded = expandedSurah,
                                            onDismissRequest = { expandedSurah = false }
                                        ) {
                                            // Loop untuk setiap surah Al-Quran
                                            surahList.forEach { surah ->
                                                DropdownMenuItem(
                                                    text = { Text("${surah.first}. ${surah.second}") }, // Format: "1. Al-Fatihah"
                                                    onClick = {
                                                        quranSurah = surah.first // Set nomor surah yang dipilih
                                                        // Reset ayat ke 1 jika ganti surah
                                                        quranAyat = 1
                                                        expandedSurah = false // Tutup dropdown
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                // Input ayat Al-Quran
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Ayat:")
                                    Spacer(Modifier.width(8.dp))
                                    // Dropdown Ayat berdasarkan surah yang dipilih
                                    var expandedAyat by remember { mutableStateOf(false) }
                                    // Dropdown untuk memilih ayat Al-Quran
                                    Box {
                                        Text(
                                            text = if (quranAyat == 0) "" else quranAyat.toString(),
                                            modifier = Modifier
                                                .width(60.dp)
                                                .clickable { expandedAyat = true } // Klik untuk membuka dropdown
                                                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.shapes.small)
                                                .padding(8.dp)
                                        )
                                        // Dropdown menu untuk pilihan ayat Al-Quran
                                        DropdownMenu(
                                            expanded = expandedAyat,
                                            onDismissRequest = { expandedAyat = false }
                                        ) {
                                            // Loop untuk setiap ayat dalam surah yang dipilih
                                            (1..ayatMax).forEach { ayat ->
                                                DropdownMenuItem(
                                                    text = { Text(ayat.toString()) },
                                                    onClick = {
                                                        quranAyat = ayat // Set nomor ayat yang dipilih
                                                        expandedAyat = false // Tutup dropdown
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Input status lulus/mengulang
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isPassed, onClick = { isPassed = true })
                            Text("Lulus", modifier = Modifier.clickable { isPassed = true })
                            Spacer(Modifier.width(16.dp))
                            RadioButton(selected = !isPassed, onClick = { isPassed = false })
                            Text("Mengulang", modifier = Modifier.clickable { isPassed = false })
                        }
                        Spacer(Modifier.height(8.dp))
                        // Input catatan guru (opsional)
                        OutlinedTextField(
                            value = catatanGuru,
                            onValueChange = { catatanGuru = it },
                            label = { Text("Catatan Guru (opsional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Tampilkan error jika ada
                        if (showInputError != null) {
                            Text(showInputError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                },
                // Tombol konfirmasi untuk menyimpan data kehadiran
                confirmButton = {
                    Button(
                        onClick = {
                            // Validasi input untuk mode Quran
                            if (!modeIqro && (quranSurah == 0 || quranAyat == 0)) {
                                showInputError = "Surah dan ayat harus dipilih saat mode Quran!"
                                return@Button
                            }
                            // Simpan data kehadiran ke ViewModel
                            selectedStudentCode?.let { code ->
                                // Submit data kehadiran dengan detail bacaan
                                viewModel.submitAttendanceWithDetail(
                                    studentCode = code,
                                    isPresent = true, // Status hadir
                                    iqroNumber = if (modeIqro) iqroNumber else null, // Jilid Iqro jika mode Iqro
                                    iqroPage = if (modeIqro) iqroPage else null, // Halaman Iqro jika mode Iqro
                                    quranSurah = if (!modeIqro) quranSurah else null, // Surah Al-Quran jika mode Quran
                                    quranAyat = if (!modeIqro) quranAyat else null, // Ayat Al-Quran jika mode Quran
                                    isPassed = isPassed, // Status lulus/mengulang
                                    catatanGuru = catatanGuru // Catatan dari guru
                                )
                            }
                            showInputDialog = false // Tutup dialog
                            showInputError = null // Reset error
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen) // Tombol hijau Islami
                    ) {
                        Text("Simpan", color = Color.White) // Text putih untuk kontras
                    }
                },
                // Tombol batal untuk menutup dialog
                dismissButton = {
                    OutlinedButton(onClick = { showInputDialog = false }) {
                        Text("Batal", color = IslamicGreen) // Text hijau untuk tombol batal
                    }
                },
                shape = RoundedCornerShape(16.dp) // Border radius yang lembut untuk dialog
            )
        }
    }
}

/**
 * AttendanceItem - Komponen untuk menampilkan item kehadiran siswa
 * Menampilkan status kehadiran dengan tombol toggle dan info bacaan
 * @param studentAttendance Data kehadiran siswa
 * @param onToggleAttendance Callback untuk toggle status kehadiran
 */
@Composable
fun AttendanceItem(
    studentAttendance: StudentAttendance,
    onToggleAttendance: () -> Unit
) {
    // Card untuk menampilkan status kehadiran siswa
    Card(
        onClick = onToggleAttendance, // Klik untuk toggle status kehadiran
        shape = RoundedCornerShape(12.dp), // Border radius yang lembut
        elevation = CardDefaults.cardElevation(4.dp), // Shadow untuk efek floating
        colors = CardDefaults.cardColors(containerColor = IvoryWhite), // Background putih gading
        modifier = Modifier.fillMaxWidth()
    ) {
        // Layout horizontal untuk nama siswa dan status kehadiran
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp), // Padding untuk spacing
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nama siswa dengan style yang sesuai
            Text(
                text = studentAttendance.student.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f) // Mengambil sisa ruang yang tersedia
            )
            // Icon status kehadiran (hadir/absen)
            if (studentAttendance.isPresent) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Hadir",
                    tint = IslamicGreen, // Warna hijau untuk status hadir
                    modifier = Modifier.size(28.dp)
                )
            } else {
                // Surface untuk status absen (lingkaran kosong)
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape, // Bentuk lingkaran
                    border = BorderStroke(2.dp, SoftGold), // Border emas untuk status absen
                    color = Color.Transparent // Background transparan
                ) { /* kosong */ }
            }
        }
    }
} 