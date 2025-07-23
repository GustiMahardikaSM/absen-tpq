package com.example.myapplication.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import com.example.myapplication.ui.theme.SoftGold
import com.example.myapplication.ui.theme.IvoryWhite
import com.example.myapplication.ui.theme.IslamicGreen
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.withStyle
import com.example.myapplication.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    MyApplicationTheme(darkTheme = false) {
        val context = LocalContext.current
        val uiState by viewModel.uiState.collectAsState()
        
        // File picker for import
        val importLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { viewModel.importData(context, it) }
        }
        
        // File picker for export
        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let { viewModel.exportData(context, it) }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pengaturan") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Export Button
                Button(
                    onClick = { 
                        exportLauncher.launch("tpq_data_${System.currentTimeMillis()}.json")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ekspor Data")
                }
                
                // Import Button
                Button(
                    onClick = { 
                        importLauncher.launch("application/json")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Impor Data")
                }
                
                // Loading indicator
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                    Text(uiState.loadingMessage ?: "Memproses...")
                }
                
                // Success/Error messages
                uiState.message?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isError) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = if (uiState.isError) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Developer Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SoftGold),
                    colors = CardDefaults.cardColors(containerColor = IvoryWhite),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    val uriHandler = LocalUriHandler.current
                    Box {
                        // Ornamen arabesque semi-transparan (pakai Icons.Default.Star)
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.TopEnd)
                                .alpha(0.08f),
                            tint = IslamicGreen
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header dengan ikon
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star, // Ganti dengan ikon masjid jika ada
                                    contentDescription = null,
                                    tint = IslamicGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Tentang Aplikasi",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicGreen
                                    )
                                )
                            }
                            Divider(color = SoftGold, thickness = 1.dp)
                            // Dibuat oleh
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SoftGold
                                    )
                                },
                                headlineContent = {
                                    Text("Dibuat oleh", style = MaterialTheme.typography.bodyMedium)
                                },
                                supportingContent = {
                                    Text(
                                        "Gusti Mahardika SM",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            textDecoration = TextDecoration.Underline,
                                            fontWeight = FontWeight.Medium,
                                            color = IslamicGreen
                                        )
                                    )
                                }
                            )
                            // Institusi
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        tint = SoftGold
                                    )
                                },
                                headlineContent = {
                                    Text("Institusi", style = MaterialTheme.typography.bodyMedium)
                                },
                                supportingContent = {
                                    Text(
                                        "Teknik Elektro UNDIP 2022",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
                                    )
                                }
                            )
                            Divider(color = SoftGold, thickness = 1.dp)
                            // Kontak
                            Text("Kontak", style = MaterialTheme.typography.titleMedium, color = IslamicGreen)
                            Spacer(Modifier.height(4.dp))
                            // LinkedIn
                            ClickableText(
                                text = buildAnnotatedString {
                                    val url = "https://www.linkedin.com/in/gusti-mahardika-sm/"
                                    pushStringAnnotation("URL", url)
                                    withStyle(SpanStyle(color = SoftGold, textDecoration = TextDecoration.Underline)) {
                                        append("LinkedIn: gusti-mahardika-sm")
                                    }
                                    pop()
                                },
                                onClick = { offset ->
                                    uriHandler.openUri("https://www.linkedin.com/in/gusti-mahardika-sm/")
                                },
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            // Instagram
                            ClickableText(
                                text = buildAnnotatedString {
                                    val url = "https://www.instagram.com/gusti_mahardika_sm/"
                                    pushStringAnnotation("URL", url)
                                    withStyle(SpanStyle(color = SoftGold, textDecoration = TextDecoration.Underline)) {
                                        append("Instagram: gusti_mahardika_sm")
                                    }
                                    pop()
                                },
                                onClick = {
                                    uriHandler.openUri("https://www.instagram.com/gusti_mahardika_sm/")
                                },
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 