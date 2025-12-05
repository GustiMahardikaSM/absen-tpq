package com.example.myapplication.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * IslamicTopAppBar - TopAppBar dengan tema Islami
 * Menggunakan warna hijau dengan gradien dan tipografi yang sesuai
 * @param title Judul yang ditampilkan di app bar
 * @param onNavigateBack Callback untuk tombol kembali (opsional)
 * @param actions Actions yang ditampilkan di sebelah kanan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Warna putih untuk kontras dengan background hijau
                )
            ) 
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = IslamicGreen, // Warna hijau Islami
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = Modifier.background(
            Brush.horizontalGradient(
                colors = listOf(IslamicGreen, IslamicGreenLight) // Gradien hijau untuk efek visual
            )
        )
    )
}

/**
 * IslamicFloatingActionButton - FloatingActionButton dengan tema Islami
 * Menggunakan warna emas dengan kontras hijau tua
 * @param onClick Callback ketika button diklik
 * @param icon Icon yang ditampilkan (default: Add icon)
 * @param modifier Modifier untuk styling
 */
@Composable
fun IslamicFloatingActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit = {
        Icon(Icons.Default.Add, contentDescription = "Tambah")
    },
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = SoftGold, // Warna emas lembut
        contentColor = DarkGreen, // Warna hijau tua untuk kontras
        shape = CircleShape, // Bentuk lingkaran
        modifier = modifier.size(64.dp) // Ukuran yang lebih besar untuk kemudahan akses
    ) {
        icon()
    }
}

/**
 * IslamicSearchBar - Search bar dengan tema Islami
 * Menggunakan warna hijau dengan border radius yang lembut
 * @param value Nilai input saat ini
 * @param onValueChange Callback ketika nilai berubah
 * @param placeholder Teks placeholder
 * @param modifier Modifier untuk styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Cari...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Cari",
                tint = IslamicGreen // Warna hijau untuk icon
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = IslamicGreen, // Border hijau saat fokus
            unfocusedBorderColor = IslamicGreen.copy(alpha = 0.5f), // Border hijau transparan saat tidak fokus
            cursorColor = IslamicGreen, // Warna kursor hijau
            focusedLabelColor = IslamicGreen, // Label hijau saat fokus
            unfocusedLabelColor = IslamicGreen.copy(alpha = 0.7f), // Label hijau transparan
            containerColor = Color.White // Background putih
        ),
        shape = RoundedCornerShape(16.dp), // Border radius yang lembut
        modifier = modifier
    )
}

/**
 * IslamicCard - Card dengan tema Islami
 * Menggunakan border radius yang lembut dan elevation yang sesuai
 * @param modifier Modifier untuk styling
 * @param content Konten yang ditampilkan dalam card
 */
@Composable
fun IslamicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp), // Border radius yang lembut
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Shadow yang sesuai
        colors = CardDefaults.cardColors(containerColor = Color.White) // Background putih bersih
    ) {
        content()
    }
}

/**
 * IslamicButton - Button dengan tema Islami
 * Menggunakan warna hijau dengan kontras putih
 * @param onClick Callback ketika button diklik
 * @param modifier Modifier untuk styling
 * @param enabled Status enabled/disabled
 * @param content Konten yang ditampilkan dalam button
 */
@Composable
fun IslamicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = IslamicGreen, // Background hijau Islami
            contentColor = Color.White // Text putih untuk kontras
        ),
        shape = RoundedCornerShape(12.dp) // Border radius yang lembut
    ) {
        content()
    }
}

/**
 * IslamicOutlinedButton - Outlined Button dengan tema Islami
 * Menggunakan border hijau dengan gradien
 * @param onClick Callback ketika button diklik
 * @param modifier Modifier untuk styling
 * @param enabled Status enabled/disabled
 * @param content Konten yang ditampilkan dalam button
 */
@Composable
fun IslamicOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = IslamicGreen // Text hijau
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(
                colors = listOf(IslamicGreen, IslamicGreenLight) // Gradien hijau untuk border
            )
        ),
        shape = RoundedCornerShape(12.dp) // Border radius yang lembut
    ) {
        content()
    }
}

/**
 * IslamicEmptyState - Empty state dengan tema Islami
 * Menampilkan pesan ketika tidak ada data dengan desain yang menarik
 * @param icon Emoji atau icon yang ditampilkan
 * @param title Judul pesan utama
 * @param subtitle Subtitle atau pesan tambahan
 * @param modifier Modifier untuk styling
 */
@Composable
fun IslamicEmptyState(
    icon: String = "📖", // Default icon buku
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ornamen geometrik sederhana dengan background emas transparan
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = SoftGold.copy(alpha = 0.2f), // Emas transparan
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = IslamicGreen, // Warna hijau untuk judul
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = IslamicGreen.copy(alpha = 0.7f) // Hijau transparan untuk subtitle
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * IslamicLoadingIndicator - Loading indicator dengan tema Islami
 * Menampilkan loading dengan warna hijau dan teks "Memuat..."
 * @param modifier Modifier untuk styling
 */
@Composable
fun IslamicLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = IslamicGreen, // Warna hijau untuk loading indicator
                modifier = Modifier.size(48.dp) // Ukuran yang sesuai
            )
            Text(
                text = "Memuat...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = IslamicGreen, // Warna hijau untuk text
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * IslamicDivider - Divider dengan tema Islami
 * Menggunakan warna hijau transparan untuk pemisah yang lembut
 * @param modifier Modifier untuk styling
 */
@Composable
fun IslamicDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier,
        color = IslamicGreen.copy(alpha = 0.2f), // Hijau transparan untuk efek lembut
        thickness = 1.dp
    )
}

/**
 * IslamicBadge - Badge dengan tema Islami
 * Menampilkan label dengan background emas dan text hijau tua
 * @param text Teks yang ditampilkan dalam badge
 * @param modifier Modifier untuk styling
 * @param color Warna background badge (default: SoftGold)
 */
@Composable
fun IslamicBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SoftGold // Default warna emas
) {
    Surface(
        modifier = modifier,
        color = color, // Background emas
        shape = RoundedCornerShape(12.dp) // Border radius yang lembut
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = DarkGreen, // Text hijau tua untuk kontras
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp) // Padding yang sesuai
        )
    }
} 