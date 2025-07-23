@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.ui.screens.student

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onViewStudentReport: (String) -> Unit,
    viewModel: ReportViewModel = viewModel()
) {
    val reportList by viewModel.reportList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    MyApplicationTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Laporan 30 Hari",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        Icon(
                            Icons.Default.Star, // Ganti dengan ikon kaligrafi jika ada
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = IslamicGreen,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = IvoryWhite
        ) { padding ->
            ReportContent(
                modifier = Modifier.padding(padding),
                reportList = reportList,
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                onViewStudentReport = onViewStudentReport
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportContent(
    modifier: Modifier = Modifier,
    reportList: List<StudentReportItem>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onViewStudentReport: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) reportList else reportList.filter { it.studentName.contains(query, true) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    Column(modifier = modifier.fillMaxSize()) {
        ReportSearchField(query) { query = it }
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = IslamicGreen)
                    }
                }
                filtered.isEmpty() -> {
                    ReportEmptyState("Belum ada data siswa")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { item ->
                            ReportItem(
                                studentName = item.studentName,
                                presentCount = item.presentCount,
                                onClick = { onViewStudentReport(item.studentCode) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSearchField(searchQuery: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(Icons.Default.Search, tint = IslamicGreen, contentDescription = null)
        },
        placeholder = { Text("Cari Nama Santri", color = Color.Gray) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = IslamicGreen,
            unfocusedBorderColor = SoftGold,
            cursorColor = IslamicGreen,
            focusedLabelColor = IslamicGreen
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ReportItem(
    studentName: String,
    presentCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = IvoryWhite),
        border = BorderStroke(1.dp, SoftGold)
    ) {
        Box {
            // Motif arabesque di pojok (pakai Icons.Default.Star semi transparan)
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .alpha(0.1f),
                tint = IslamicGreen
            )

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
                    color = IslamicGreen
                )
                Text(
                    text = "Hadir: $presentCount hari",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReportEmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
// Ganti/extend sesuai kebutuhan data Anda

data class ReportItemData(
    val studentId: Long,
    val studentName: String,
    val presentCount: Int
) 