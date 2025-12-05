package com.example.myapplication.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// Screen untuk menambah atau mengedit data siswa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditStudentViewModel = viewModel()
) {
    // scope untuk coroutine (mis. ketika menyimpan data)
    val scope = rememberCoroutineScope()
    // state untuk menampilkan dialog error ketika validasi gagal
    var showErrorDialog by remember { mutableStateOf(false) }

    // Dialog error jika nama siswa kosong atau validasi gagal
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text("Nama siswa tidak boleh kosong") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Scaffold berisi AppBar, konten utama, dan handling padding
    Scaffold(
        topBar = {
            TopAppBar(
                // Judul berbeda tergantung apakah menambah atau mengedit (dilihat dari nama)
                title = { Text(if (viewModel.studentName.isEmpty()) "Tambah Siswa" else "Edit Siswa") },
                navigationIcon = {
                    // Tombol kembali (navigasi)
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        // Jika sedang memuat data tampilkan loading indicator
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator() // progress tengah layar
            }
        } else {
            // Kolom utama form input siswa
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Field input nama siswa
                OutlinedTextField(
                    value = viewModel.studentName,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nama Siswa") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Bagian pilihan jenis kelamin (radio buttons)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Jenis Kelamin:")
                    Spacer(Modifier.width(8.dp))
                    RadioButton(
                        selected = viewModel.gender == "Laki-laki",
                        onClick = { viewModel.onGenderChange("Laki-laki") }
                    )
                    Text("Laki-laki")
                    Spacer(Modifier.width(8.dp))
                    RadioButton(
                        selected = viewModel.gender == "Perempuan",
                        onClick = { viewModel.onGenderChange("Perempuan") }
                    )
                    Text("Perempuan")
                }

                // Tanggal Lahir: tombol untuk membuka date picker
                var showDatePicker by remember { mutableStateOf(false) }
                // Format tampilan tanggal jika sudah diisi, atau teks placeholder
                val birthDateText = viewModel.birthDate?.let { java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(it)) } ?: "Pilih Tanggal Lahir"
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(birthDateText)
                }
                // DatePickerDialog ketika showDatePicker = true
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = viewModel.birthDate ?: System.currentTimeMillis()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            // Konfirmasi pilih tanggal -> update ke viewModel
                            TextButton(onClick = {
                                viewModel.onBirthDateChange(datePickerState.selectedDateMillis)
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                        }
                    ) {
                        DatePicker(state = datePickerState) // komponen pemilih tanggal
                    }
                }

                // Posisi belajar: Iqro atau Quran (radio buttons)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Posisi:")
                    Spacer(Modifier.width(8.dp))
                    RadioButton(
                        selected = viewModel.positionType == "Iqro",
                        onClick = { viewModel.onPositionTypeChange("Iqro") }
                    )
                    Text("Iqro")
                    Spacer(Modifier.width(8.dp))
                    RadioButton(
                        selected = viewModel.positionType == "Quran",
                        onClick = { viewModel.onPositionTypeChange("Quran") }
                    )
                    Text("Quran")
                }

                // Jika posisi adalah Iqro -> tampilkan pilihan tingkatan dan halaman
                if (viewModel.positionType == "Iqro") {
                    // Pilih tingkat Iqro / Qiroati (Pra-TK, 1-6) menggunakan dropdown
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Iqro / Qiroati:")
                        Spacer(Modifier.width(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        val tingkatList = listOf("Pra-TK", "1", "2", "3", "4", "5", "6")
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(
                                    when (viewModel.iqroNumber) {
                                        0 -> "Pilih Tingkatan"
                                        1,2,3,4,5,6 -> tingkatList[viewModel.iqroNumber]
                                        else -> tingkatList[0]
                                    }
                                )
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                tingkatList.forEachIndexed { idx, label ->
                                    DropdownMenuItem(onClick = {
                                        // saat memilih tingkat -> update viewModel
                                        viewModel.onIqroNumberChange(idx)
                                        expanded = false
                                    }, text = { Text(label) })
                                }
                            }
                        }
                    }
                    // Input halaman iqro (nomor halaman)
                    OutlinedTextField(
                        value = viewModel.iqroPage?.toString() ?: "",
                        onValueChange = { v ->
                            if (v.isBlank()) viewModel.onIqroPageChange(null)
                            else v.toIntOrNull()?.let { viewModel.onIqroPageChange(it) }
                        },
                        label = { Text("Halaman") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Jika posisi adalah Quran -> pilih surah dan ayat
                    // Daftar surah beserta jumlah ayatnya
                    val surahData = listOf(
                        "Al-Fatihah" to 7, "Al-Baqarah" to 286, "Ali-Imran" to 200, "An-Nisa" to 176,
                        "Al-Maidah" to 120, "Al-Anam" to 165, "Al-Araf" to 206, "Al-Anfal" to 75,
                        "At-Taubah" to 129, "Yunus" to 109, "Hud" to 123, "Yusuf" to 111,
                        "Ar-Rad" to 43, "Ibrahim" to 52, "Al-Hijr" to 99, "An-Nahl" to 128,
                        "Al-Isra" to 111, "Al-Kahfi" to 110, "Maryam" to 98, "Ta Ha" to 135,
                        "Al-Anbiya" to 112, "Al-Hajj" to 78, "Al-Muminun" to 118, "An-Nur" to 64,
                        "Al-Furqan" to 77, "Asy-Syuara" to 227, "An-Naml" to 93, "Al-Qasas" to 88,
                        "Al-Ankabut" to 69, "Ar-Ruum" to 60, "Luqman" to 34, "As-Sajdah" to 30,
                        "Al-Ahzab" to 73, "Saba" to 54, "Fatir" to 45, "Ya-Sin" to 83,
                        "Ash-Shaffat" to 182, "Shad" to 88, "Az-Zumar" to 75, "Gafir" to 85,
                        "Fushshilat" to 54, "Asy-Syura" to 53, "Az-Zukhruf" to 89, "Ad-Dukhan" to 59,
                        "Al-Jatsiyah" to 37, "Al-Ahqaf" to 35, "Muhammad" to 38, "Al-Fath" to 29,
                        "Al-Hujurat" to 18, "Qaf" to 45, "Adz-Dzariyat" to 60, "Ath-Thuur" to 49,
                        "An-Najm" to 62, "Al-Qamar" to 55, "Ar-Rahman" to 78, "Al-Waqiah" to 96,
                        "Al-Hadid" to 29, "Al-Mujadilah" to 22, "Al-Hasyr" to 24, "Al-Mumtahanah" to 13,
                        "Ash-Shaf" to 14, "Al-Jumuah" to 11, "Al-Munafiqun" to 11, "At-Taghabun" to 18,
                        "Ath-Thalaq" to 12, "At-Tahrim" to 12, "Al-Mulk" to 30, "Al-Qalam" to 52,
                        "Al-Haqqah" to 52, "Al-Maarij" to 44, "Nuh" to 28, "Al-Jin" to 28,
                        "Al-Muzammil" to 20, "Al-Muddatstsir" to 56, "Al-Qiyamah" to 40, "Al-Insan" to 31,
                        "Al-Mursalat" to 50, "An-Naba" to 40, "An-Naziat" to 46, "Abasa" to 42,
                        "At-Takwir" to 29, "Al-Infithar" to 19, "Al-Muthaffifin" to 36, "Al-Inshiqaq" to 25,
                        "Al-Buruj" to 22, "Ath-Thariq" to 17, "Al-Alaa" to 19, "Al-Ghasyiyah" to 26,
                        "Al-Fajr" to 30, "Al-Balad" to 20, "Asy-Syams" to 15, "Al-Lail" to 21,
                        "Adh-Dhuha" to 11, "Al-Inshirah" to 8, "At-Tin" to 8, "Al-Alaq" to 19,
                        "Al-Qadr" to 5, "Al-Bayyinah" to 8, "Al-Zalzalah" to 8, "Al-Adiyat" to 11,
                        "Al-Qariah" to 11, "At-Takatsur" to 8, "Al-Ashr" to 3, "Al-Humazah" to 9,
                        "Al-Fil" to 5, "Quraysh" to 4, "Al-Maun" to 7, "Al-Kautsar" to 3,
                        "Al-Kafirun" to 6, "An-Nashr" to 3, "Al-Lahab" to 5, "Al-Ikhlas" to 4,
                        "Al-Falaq" to 5, "An-Nas" to 6
                    )

                    // Dropdown Surah
                    Text("Pilih Surah:", style = MaterialTheme.typography.bodySmall)
                    var surahExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { surahExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            val current = if (viewModel.quranSurah in 1..surahData.size) viewModel.quranSurah else 1
                            Text("$current. ${surahData[current - 1].first}")
                        }
                        DropdownMenu(expanded = surahExpanded, onDismissRequest = { surahExpanded = false }) {
                            surahData.forEachIndexed { index, (name, _) ->
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.onQuranSurahChange(index + 1)
                                        // Reset ayat ke 1 saat ganti surah
                                        viewModel.onQuranAyatChange(1)
                                        surahExpanded = false
                                    },
                                    text = { Text("${index + 1}. $name") }
                                )
                            }
                        }
                    }

                    // Dropdown Ayat (setelah surah dipilih)
                    if (viewModel.quranSurah in 1..surahData.size) {
                        Text("Pilih Ayat:", style = MaterialTheme.typography.bodySmall)
                        val ayatCount = surahData[viewModel.quranSurah - 1].second
                        var ayatExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { ayatExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(viewModel.quranAyat.coerceIn(1, ayatCount).toString())
                            }
                            DropdownMenu(expanded = ayatExpanded, onDismissRequest = { ayatExpanded = false }) {
                                (1..ayatCount).forEach { ayatNum ->
                                    DropdownMenuItem(onClick = {
                                        viewModel.onQuranAyatChange(ayatNum)
                                        ayatExpanded = false
                                    }, text = { Text(ayatNum.toString()) })
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (viewModel.saveStudent()) {
                                onNavigateBack()
                            } else {
                                showErrorDialog = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan")
                }
            }
        }
    }
} 