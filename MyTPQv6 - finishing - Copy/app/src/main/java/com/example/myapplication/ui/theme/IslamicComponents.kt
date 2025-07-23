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
 * TopAppBar dengan tema Islami
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
                    color = Color.White
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
            containerColor = IslamicGreen,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = Modifier.background(
            Brush.horizontalGradient(
                colors = listOf(IslamicGreen, IslamicGreenLight)
            )
        )
    )
}

/**
 * FloatingActionButton dengan tema Islami
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
        containerColor = SoftGold,
        contentColor = DarkGreen,
        shape = CircleShape,
        modifier = modifier.size(64.dp)
    ) {
        icon()
    }
}

/**
 * Search Bar dengan tema Islami
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
                tint = IslamicGreen
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = IslamicGreen,
            unfocusedBorderColor = IslamicGreen.copy(alpha = 0.5f),
            cursorColor = IslamicGreen,
            focusedLabelColor = IslamicGreen,
            unfocusedLabelColor = IslamicGreen.copy(alpha = 0.7f),
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    )
}

/**
 * Card dengan tema Islami
 */
@Composable
fun IslamicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        content()
    }
}

/**
 * Button dengan tema Islami
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
            containerColor = IslamicGreen,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        content()
    }
}

/**
 * Outlined Button dengan tema Islami
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
            contentColor = IslamicGreen
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(
                colors = listOf(IslamicGreen, IslamicGreenLight)
            )
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        content()
    }
}

/**
 * Empty State dengan tema Islami
 */
@Composable
fun IslamicEmptyState(
    icon: String = "📖",
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
            // Ornamen geometrik sederhana
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = SoftGold.copy(alpha = 0.2f),
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
                    color = IslamicGreen,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = IslamicGreen.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Loading indicator dengan tema Islami
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
                color = IslamicGreen,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Memuat...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = IslamicGreen,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Divider dengan tema Islami
 */
@Composable
fun IslamicDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier,
        color = IslamicGreen.copy(alpha = 0.2f),
        thickness = 1.dp
    )
}

/**
 * Badge dengan tema Islami
 */
@Composable
fun IslamicBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SoftGold
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = DarkGreen,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
} 