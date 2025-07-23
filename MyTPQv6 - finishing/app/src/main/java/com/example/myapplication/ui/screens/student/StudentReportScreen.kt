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
import android.graphics.BitmapFactory

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
                        Text("Tingkat Bacaan: ${detail.readingTypeSummary}")
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

    // Paints
    val titleTypeface = try {
        Typeface.createFromAsset(context.assets, "fonts/scheherazade_regular.ttf")
    } catch (e: Exception) {
        Typeface.SERIF
    }
    val titlePaint = Paint().apply {
        textSize = 24f
        isFakeBoldText = true
        typeface = titleTypeface
        color = Color.rgb(0, 100, 0) // hijau tua
    }
    val borderPaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        typeface = Typeface.SERIF
        color = Color.rgb(0, 100, 0) // hijau tua (pengganti emas)
    }
    val subTitlePaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        typeface = Typeface.SERIF
        color = Color.rgb(0, 100, 0) // hijau tua (pengganti emas)
    }
    val normalPaint = Paint().apply {
        textSize = 14f
        typeface = Typeface.SERIF
        color = Color.BLACK
    }
    val greenPaint = Paint().apply {
        textSize = 14f
        color = Color.rgb(0, 150, 0)
        typeface = Typeface.SERIF
    }
    val linePaint = Paint().apply {
        color = Color.rgb(0, 100, 0) // hijau tua (pengganti emas lembut)
        strokeWidth = 2f
    }
    val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.rgb(0, 100, 0) // hijau tua (pengganti emas)
    }
    val cardBgPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.argb(20, 0, 100, 0) // hijau muda transparan
    }

    // Logo header
    val headerLogo = try {
        BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_info_details)
    } catch (e: Exception) { null }

    var pageNumber = 1
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    var y = 50f
    val left = 40f
    val right = 555f

    // Header logo
    headerLogo?.let {
        canvas.drawBitmap(it, right - 60f, 20f, normalPaint)
    }
    // Judul
    canvas.drawText("LAPORAN PERKEMBANGAN SISWA TPQ", left, y, titlePaint)
    y += 40f

    // Box identitas
    val boxTop = y - 10f
    canvas.drawRect(left - 5f, boxTop, right + 5f, y + 80f, boxPaint)
    canvas.drawText("Nama            : ${detail.name}", left, y, normalPaint)
    y += 20f
    canvas.drawText("Tingkat Bacaan  : ${detail.readingTypeSummary}", left, y, normalPaint)
    y += 20f
    canvas.drawText("Tanggal Lahir   : ${detail.birthDate}", left, y, normalPaint)
    y += 20f
    canvas.drawText("Jenis Kelamin   : ${detail.gender}", left, y, normalPaint)
    y += 30f

    // Garis hijau tua pemisah
    canvas.drawLine(left, y, right, y, borderPaint)
    y += 24f

    // Statistik Kehadiran
    canvas.drawText("STATISTIK KEHADIRAN (30 HARI)", left, y, borderPaint)
    y += 28f
    canvas.drawText("• Hadir         : ${detail.attendanceCount} hari", left, y, normalPaint)
    y += 24f

    canvas.drawLine(left, y, right, y, borderPaint)
    y += 24f

    // Perkembangan Bacaan
    canvas.drawText("PERKEMBANGAN BACAAN", left, y, borderPaint)
    y += 28f
    canvas.drawText("• Awal 30 hari lalu: ${detail.startReading}", left, y, normalPaint)
    y += 20f
    canvas.drawText("• Saat ini        : ${detail.currentReading}", left, y, normalPaint)
    y += 30f

    canvas.drawLine(left, y, right, y, borderPaint)
    y += 24f

    // Prestasi
    canvas.drawText("PRESTASI", left, y, borderPaint)
    y += 28f
    canvas.drawText("• Total Lulus    : ${detail.totalPassed}", left, y, normalPaint)
    y += 20f
    canvas.drawText("• Total Mengulang: ${detail.totalRetake}", left, y, normalPaint)
    y += 30f

    // Laporan Harian
    canvas.drawLine(left, y, right, y, borderPaint)
    y += 24f
    canvas.drawText("Laporan Harian", left, y, borderPaint)
    y += 28f
    detail.dailyReports.forEach { daily ->
        if (y > 780f) {
            pdfDocument.finishPage(page)
            pageNumber++
            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(newPageInfo)
            canvas = page.canvas
            y = 50f
            canvas.drawText("Laporan Harian (lanjutan)", left, y, borderPaint)
            y += 28f
        }
        // Card background
        canvas.drawRoundRect(left, y, right, y + 80f, 8f, 8f, cardBgPaint)
        canvas.drawRect(left, y, right, y + 80f, boxPaint)
        canvas.drawText("${daily.date.dayOfMonth.toString().padStart(2, '0')}/${daily.date.monthValue.toString().padStart(2, '0')}/${daily.date.year}", left + 8f, y + 20f, normalPaint)
        canvas.drawText("Bacaan: ${daily.reading}", left + 8f, y + 40f, normalPaint)
        canvas.drawText("Status: ${daily.status}", left + 8f, y + 60f, if (daily.status == "Lulus") greenPaint else normalPaint)
        canvas.drawText("Catatan: ${daily.note}", left + 8f, y + 75f, normalPaint)
        y += 90f
    }

    // Footer
    canvas.drawLine(left, y, right, y, borderPaint)
    y += 20f
    normalPaint.textAlign = Paint.Align.CENTER
    canvas.drawText("Dicetak pada: ${detail.generatedAt}", (left + right) / 2, y, normalPaint)
    y += 30f
    canvas.drawText("TPQ MyTPQ", (left + right) / 2, y, borderPaint)

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