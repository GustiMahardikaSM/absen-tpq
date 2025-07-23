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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditStudentViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.studentName.isEmpty()) "Tambah Siswa" else "Edit Siswa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.studentName,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nama Siswa") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender
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

                // Tanggal Lahir
                var showDatePicker by remember { mutableStateOf(false) }
                val birthDateText = viewModel.birthDate?.let { java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(it)) } ?: "Pilih Tanggal Lahir"
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(birthDateText)
                }
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = viewModel.birthDate ?: System.currentTimeMillis()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.onBirthDateChange(datePickerState.selectedDateMillis)
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Posisi: Iqro atau Quran
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

                if (viewModel.positionType == "Iqro") {
                    // Pilih Iqro / Qiroati (Pra-TK, 1-6)
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
                                        viewModel.onIqroNumberChange(idx)
                                        expanded = false
                                    }, text = { Text(label) })
                                }
                            }
                        }
                    }
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
                    // Pilih Surah dan Ayat
                    val surahList = listOf(
                        "Al-Fatihah", "Al-Baqarah", "Ali-Imran", "An-Nisa", "Al-Maidah", "Al-Anam", "Al-Araf", "Al-Anfal", "At-Taubah", "Yunus", "Hud", "Yusuf", "Ar-Rad", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahfi", "Maryam", "Ta Ha", "Al-Anbiya", "Al-Hajj", "Al-Muminun", "An-Nur", "Al-Furqan", "Asy-Syuara", "An-Naml", "Al-Qasas", "Al-Ankabut", "Ar-Ruum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "Ash-Shaffat", "Shad", "Az-Zumar", "Gafir", "Fushshilat", "Asy-Syura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jatsiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adz-Dzariyat", "Ath-Thuur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqiah", "Al-Hadid", "Al-Mujadilah", "Al-Hasyr", "Al-Mumtahanah", "Ash-Shaf", "Al-Jumuah", "Al-Munafiqun", "At-Taghabun", "Ath-Thalaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Maarij", "Nuh", "Al-Jin", "Al-Muzammil", "Al-Muddatstsir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Naziat", "Abasa", "At-Takwir", "Al-Infithar", "Al-Muthaffifin", "Al-Inshiqaq", "Al-Buruj", "Ath-Thariq", "Al-Alaa", "Al-Ghasyiyah", "Al-Fajr", "Al-Balad", "Asy-Syams", "Al-Lail", "Adh-Dhuha", "Al-Inshirah", "At-Tin", "Al-Alaq", "Al-Qadr", "Al-Bayyinah", "Al-Zalzalah", "Al-Adiyat", "Al-Qariah", "At-Takatsur", "Al-Ashr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Maun", "Al-Kautsar", "Al-Kafirun", "An-Nashr", "Al-Lahab", "Al-Ikhlas", "Al-Falaq", "An-Nas"
                    )
                    val surahAyatCounts = listOf(
                        7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,54,53,89,59,37,35,38,29,18,45,60,49,62,55,78,96,29,22,24,13,14,11,11,18,12,12,30,52,52,44,28,28,20,56,40,31,50,40,46,42,29,19,36,25,22,17,19,26,30,20,15,21,11,8,8,19,5,8,8,11,11,3,9,5,4,7,3,6
                    )
                    var expanded by remember { mutableStateOf(false) }
                    // Surah: default 'Pilih Surah' jika quranSurah == 0
                    Text("Pilih Surah:", style = MaterialTheme.typography.bodySmall)
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (viewModel.quranSurah == 0) "Pilih Surah" else "${viewModel.quranSurah}. ${surahList[viewModel.quranSurah - 1]}")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            surahList.forEachIndexed { idx, name ->
                                DropdownMenuItem(onClick = {
                                    viewModel.onQuranSurahChange(idx + 1)
                                    viewModel.onQuranAyatChange(1)
                                    expanded = false
                                }, text = { Text("${idx + 1}. $name") })
                            }
                        }
                    }
                    // Dropdown Ayat: hanya tampil jika surah sudah dipilih
                    if (viewModel.quranSurah > 0) {
                        Text("Pilih Ayat:", style = MaterialTheme.typography.bodySmall)
                        val ayatCount = surahAyatCounts[viewModel.quranSurah - 1]
                        var ayatExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { ayatExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(viewModel.quranAyat.toString())
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