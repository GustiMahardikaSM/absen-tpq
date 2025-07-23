package com.example.myapplication.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.ClickableText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                val uriHandler = LocalUriHandler.current
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tentang Aplikasi",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Aplikasi ini dibuat oleh:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Gusti Mahardika SM",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Teknik Elektro UNDIP 2022",
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Kontak:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    ClickableText(
                        text = buildAnnotatedString {
                            val url = "https://www.linkedin.com/in/gusti-mahardika-sm/"
                            pushStringAnnotation(tag = "URL", annotation = url)
                            pushStyle(SpanStyle(color = Color(0xFF1565C0), textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic))
                            append("LinkedIn: linkedin.com/in/gusti-mahardika-sm")
                            pop()
                            pop()
                        },
                        onClick = { offset ->
                            val annotatedString = buildAnnotatedString {
                                val url = "https://www.linkedin.com/in/gusti-mahardika-sm/"
                                pushStringAnnotation(tag = "URL", annotation = url)
                                append("")
                                pop()
                            }
                            val annotations = annotatedString.getStringAnnotations("URL", offset, offset)
                            annotations.firstOrNull()?.let { sa ->
                                uriHandler.openUri(sa.item)
                            } ?: uriHandler.openUri("https://www.linkedin.com/in/gusti-mahardika-sm/")
                        },
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    ClickableText(
                        text = buildAnnotatedString {
                            val url = "https://www.instagram.com/gusti_mahardika_sm/"
                            pushStringAnnotation(tag = "URL", annotation = url)
                            pushStyle(SpanStyle(color = Color(0xFF1565C0), textDecoration = TextDecoration.Underline, fontStyle = FontStyle.Italic))
                            append("Instagram: instagram.com/gusti_mahardika_sm")
                            pop()
                            pop()
                        },
                        onClick = { offset ->
                            val annotatedString = buildAnnotatedString {
                                val url = "https://www.instagram.com/gusti_mahardika_sm/"
                                pushStringAnnotation(tag = "URL", annotation = url)
                                append("")
                                pop()
                            }
                            val annotations = annotatedString.getStringAnnotations("URL", offset, offset)
                            annotations.firstOrNull()?.let { sa ->
                                uriHandler.openUri(sa.item)
                            } ?: uriHandler.openUri("https://www.instagram.com/gusti_mahardika_sm/")
                        },
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
} 