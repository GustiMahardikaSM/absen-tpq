package com.example.myapplication.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import android.content.Context
import android.os.Environment
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.graphics.Typeface
import android.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudentReportViewModel = viewModel()
) {
    val reportDetail by viewModel.reportDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Perkembangan Siswa TPQ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        val context = LocalContext.current
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reportDetail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Siswa tidak ditemukan")
            }
        } else {
            val detail = reportDetail!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { generateAndSharePdf(context, detail, share = false) }) {
                        Text("Unduh PDF")
                    }
                    Button(onClick = { generateAndSharePdf(context, detail, share = true) }) {
                        Text("Bagikan PDF")
                    }
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Nama: ${detail.name}")
                        Text("Tingkat Bacaan: ${detail.level}")
                        Text("Tanggal Lahir: ${detail.birthDate}")
                        Text("Jenis Kelamin: ${detail.gender}")
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("STATISTIK KEHADIRAN (30 HARI TERAKHIR)", style = MaterialTheme.typography.titleMedium)
                        Text("- Hadir: ${detail.attendanceCount}")
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("PERKEMBANGAN BACAAN", style = MaterialTheme.typography.titleMedium)
                        Text("- Posisi Awal (30 hari lalu): ${detail.startReading}")
                        Text("- Posisi Saat Ini: ${detail.currentReading}")
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("PRESTASI", style = MaterialTheme.typography.titleMedium)
                        Text("- Total Lulus: ${detail.totalPassed}")
                        Text("- Total Mengulang: ${detail.totalRetake}")
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Laporan harian", style = MaterialTheme.typography.titleMedium)
                    }
                    items(detail.dailyReports) { daily ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("tanggal :${daily.date.dayOfMonth}/${daily.date.monthValue}/${daily.date.year}")
                                Text("bacaan : ${daily.reading}")
                                Text("lulus/ulang : ${daily.status}")
                                Text("catatan guru: ${daily.note}")
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Laporan dibuat pada: ${detail.generatedAt}")
                    }
                }
            }
        }
    }
}

fun generateAndSharePdf(context: Context, detail: StudentReportDetail, share: Boolean) {
    val dateParts = detail.generatedAt.split("/")
    val day = dateParts.getOrNull(0)?.padStart(2, '0') ?: "00"
    val month = dateParts.getOrNull(1)?.padStart(2, '0') ?: "00"
    val year = dateParts.getOrNull(2) ?: "0000"
    val dateStr = "$day$month$year"
    val fileName = "laporan_${detail.name.replace(" ", "_").lowercase()}_${dateStr}.pdf"
    val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val pdfFile = File(downloadDir, fileName)
    val pdfDocument = PdfDocument()
    val titlePaint = Paint().apply { textSize = 22f; isFakeBoldText = true; typeface = Typeface.SANS_SERIF }
    val subTitlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true; typeface = Typeface.SANS_SERIF }
    val normalPaint = Paint().apply { textSize = 13f; typeface = Typeface.SANS_SERIF }
    val greenPaint = Paint().apply { textSize = 13f; color = Color.rgb(0, 150, 0); typeface = Typeface.SANS_SERIF }
    val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 2f }

    var pageNumber = 1
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    var y = 50f
    val left = 40f
    val right = 555f

    // Judul
    canvas.drawText("LAPORAN PERKEMBANGAN SISWA TPQ", left, y, titlePaint)
    y += 36f
    // Identitas
    canvas.drawText("Nama: ${detail.name}", left, y, normalPaint)
    y += 20f
    canvas.drawText("Tingkat Bacaan: ${detail.level}", left, y, normalPaint)
    y += 20f
    canvas.drawText("Tanggal Lahir: ${detail.birthDate}", left, y, normalPaint)
    y += 20f
    canvas.drawText("Jenis Kelamin: ${detail.gender}", left, y, normalPaint)
    y += 16f
    // Garis horizontal
    y += 4f
    canvas.drawLine(left, y, right, y, linePaint)
    y += 20f
    // Statistik Kehadiran
    canvas.drawText("STATISTIK KEHADIRAN (30 HARI TERAKHIR)", left, y, subTitlePaint)
    y += 22f
    canvas.drawText("- Hadir: ${detail.attendanceCount}", left, y, normalPaint)
    y += 12f
    y += 4f
    canvas.drawLine(left, y, right, y, linePaint)
    y += 20f
    // Perkembangan Bacaan
    canvas.drawText("PERKEMBANGAN BACAAN", left, y, subTitlePaint)
    y += 22f
    canvas.drawText("- Posisi Awal (30 hari lalu): ${detail.startReading}", left, y, normalPaint)
    y += 20f
    canvas.drawText("- Posisi Saat Ini: ${detail.currentReading}", left, y, normalPaint)
    y += 12f
    y += 4f
    canvas.drawLine(left, y, right, y, linePaint)
    y += 20f
    // Prestasi
    canvas.drawText("PRESTASI", left, y, subTitlePaint)
    y += 22f
    canvas.drawText("- Total Lulus: ${detail.totalPassed}", left, y, normalPaint)
    y += 20f
    canvas.drawText("- Total Mengulang: ${detail.totalRetake}", left, y, normalPaint)
    y += 12f
    y += 4f
    canvas.drawLine(left, y, right, y, linePaint)
    y += 24f
    // Laporan Harian
    canvas.drawText("Laporan harian", left, y, subTitlePaint)
    y += 24f
    detail.dailyReports.forEach { daily ->
        if (y > 800f) {
            pdfDocument.finishPage(page)
            pageNumber++
            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(newPageInfo)
            canvas = page.canvas
            y = 50f
            canvas.drawText("Laporan harian (lanjutan)", left, y, subTitlePaint)
            y += 24f
        }
        // Garis untuk setiap hari
        canvas.drawLine(left, y, right, y, linePaint)
        y += 12f
        canvas.drawText("Tanggal : ${daily.date.dayOfMonth}/${daily.date.monthValue}/${daily.date.year}", left, y, normalPaint)
        y += 18f
        canvas.drawText("Bacaan : ${daily.reading}", left, y, normalPaint)
        y += 18f
        if (daily.status == "Lulus") {
            canvas.drawText("Lulus/Ulang : Lulus ✅", left, y, greenPaint)
        } else {
            canvas.drawText("Lulus/Ulang : ${daily.status}", left, y, normalPaint)
        }
        y += 18f
        canvas.drawText("Catatan guru: ${daily.note}", left, y, normalPaint)
        y += 12f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 24f
    }
    canvas.drawText("Laporan dibuat pada: ${detail.generatedAt}", left, y, normalPaint)
    pdfDocument.finishPage(page)

    // Simpan ke file
    try {
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return
    } finally {
        pdfDocument.close()
    }

    if (share) {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan PDF"))
    } else {
        android.widget.Toast.makeText(context, "PDF berhasil disimpan di folder Download: ${pdfFile.name}", android.widget.Toast.LENGTH_LONG).show()
    }
} 