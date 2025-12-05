@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.ui.screens.student

// Import library Compose dan utilitas UI
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// Komposabel utama layar laporan siswa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onViewStudentReport: (String) -> Unit, // callback untuk membuka detail laporan siswa
    viewModel: ReportViewModel = viewModel() // ViewModel yang menyimpan state laporan
) {
    // Ambil state dari ViewModel
    val reportList by viewModel.reportList.collectAsState() // daftar laporan siswa
    val isLoading by viewModel.isLoading.collectAsState() // flag loading awal
    val isRefreshing by viewModel.isRefreshing.collectAsState() // flag refresh pull-to-refresh

    // Theme aplikasi (pakai tema custom MyApplicationTheme)
    MyApplicationTheme(darkTheme = false) {
        // Scaffold untuk struktur dasar halaman (top bar, body)
        Scaffold(
            topBar = {
                // TopAppBar menampilkan judul dan ikon
                TopAppBar(
                    title = {
                        Text(
                            "Laporan 30 Hari", // judul layar
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        Icon(
                            Icons.Default.Star, // ikon dekoratif (bisa diganti)
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = IslamicGreen, // warna background top bar
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = IvoryWhite // warna latar scaffold
        ) { padding ->
            // Konten utama layar: search + daftar laporan
            ReportContent(
                modifier = Modifier.padding(padding),
                reportList = reportList,
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() }, // aksi refresh data
                onViewStudentReport = onViewStudentReport // forward callback
            )
        }
    }
}

// Komposabel konten utama: field pencarian + list dengan swipe-to-refresh
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportContent(
    modifier: Modifier = Modifier,
    reportList: List<StudentReportItem>, // data laporan yang ditampilkan
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onViewStudentReport: (String) -> Unit
) {
    var query by remember { mutableStateOf("") } // state untuk query pencarian
    // Filter daftar berdasarkan query (case-insensitive)
    val filtered = if (query.isBlank()) reportList else reportList.filter { it.studentName.contains(query, true) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing) // state untuk swipe refresh

    Column(modifier = modifier.fillMaxSize()) {
        // Field pencarian di atas
        ReportSearchField(query) { query = it }

        // SwipeRefresh membungkus area list untuk pull-to-refresh
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh
        ) {
            when {
                isLoading -> {
                    // Tampilkan loading saat data sedang dimuat
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = IslamicGreen)
                    }
                }
                filtered.isEmpty() -> {
                    // Tampilkan state kosong jika tidak ada data
                    ReportEmptyState("Belum ada data siswa")
                }
                else -> {
                    // Tampilkan daftar laporan dalam LazyColumn
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { item ->
                            // Item kartu laporan per siswa
                            ReportItem(
                                studentName = item.studentName,
                                presentCount = item.presentCount,
                                onClick = { onViewStudentReport(item.studentCode) } // buka laporan detail
                            )
                        }
                    }
                }
            }
        }
    }
}

// Komposabel field pencarian dengan OutlinedTextField
@Composable
fun ReportSearchField(searchQuery: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        leadingIcon = {
            // Ikon kaca pembesar di kiri field
            Icon(Icons.Default.Search, tint = IslamicGreen, contentDescription = null)
        },
        placeholder = { Text("Cari Nama Santri", color = Color.Gray) }, // placeholder teks
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = IslamicGreen, // warna border saat fokus
            unfocusedBorderColor = SoftGold,
            cursorColor = IslamicGreen,
            focusedLabelColor = IslamicGreen
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// Komposabel untuk tiap kartu item laporan siswa
@Composable
fun ReportItem(
    studentName: String,
    presentCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // klik buka detail
        shape = RoundedCornerShape(16.dp), // rounded corner kartu
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = IvoryWhite), // warna background kartu
        border = BorderStroke(1.dp, SoftGold) // border dekoratif
    ) {
        Box {
            // Motif arabesque di pojok (ikon bintang semi transparan) sebagai latar dekoratif
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .alpha(0.1f), // transparansi rendah
                tint = IslamicGreen
            )

            // Baris utama: nama siswa di kiri, jumlah hari hadir di kanan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGreen // warna teks nama
                )
                Text(
                    text = "Hadir: $presentCount hari",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // warna teks sekunder
                )
            }
        }
    }
}

// Komposabel state kosong (empty state) saat tidak ada data
@Composable
fun ReportEmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Ikon besar untuk menandai tidak ada data
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = SoftGold,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    }
}

// Data class untuk item laporan
// Catatan: nama data ini berbeda dengan komposabel ReportItem untuk menghindari konflik nama
// Ganti/extend sesuai kebutuhan data Anda (misal menambah studentCode)
data class ReportItemData(
    val studentId: Long,
    val studentName: String,
    val presentCount: Int
)