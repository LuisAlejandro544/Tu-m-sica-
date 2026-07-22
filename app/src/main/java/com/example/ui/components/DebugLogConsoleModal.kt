package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite
import com.example.util.DebugLogger
import com.example.util.LogEntry
import com.example.util.LogType
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun FloatingDebugButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Clamp bounds so button can NEVER be lost off screen or stuck at top
                    offsetX = (offsetX + dragAmount.x).coerceIn(-320f, 20f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(0f, 1200f)
                }
            }
            .testTag("debug_log_fab")
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFFE91E63))
            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = "Abrir Consola de Logs Debug",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun DebugLogConsoleModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val logs by DebugLogger.logs.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf<LogType?>(null) } // null = ALL

    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == null) logs
        else logs.filter { it.type == selectedFilter }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("debug_log_console_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SpotifyBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Consola de Logs (APK Debug)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextWhite
                            )
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SpotLocal Logs", DebugLogger.getAllLogsFormattedText())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Logs copiados al portapapeles", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = SpotifyGreen)
                        }

                        IconButton(onClick = { DebugLogger.clearLogs() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpiar", tint = Color.Red)
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Clear, contentDescription = "Cerrar", tint = SpotifyTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        label = "Todos (${logs.size})",
                        isSelected = selectedFilter == null,
                        color = SpotifyGreen,
                        onClick = { selectedFilter = null }
                    )
                    FilterChip(
                        label = "Acciones (${logs.count { it.type == LogType.ACTION }})",
                        isSelected = selectedFilter == LogType.ACTION,
                        color = Color(0xFF2196F3),
                        onClick = { selectedFilter = LogType.ACTION }
                    )
                    FilterChip(
                        label = "Warnings (${logs.count { it.type == LogType.WARNING }})",
                        isSelected = selectedFilter == LogType.WARNING,
                        color = Color(0xFFFFC107),
                        onClick = { selectedFilter = LogType.WARNING }
                    )
                    FilterChip(
                        label = "Crashes (${logs.count { it.type == LogType.CRASH_ERROR }})",
                        isSelected = selectedFilter == LogType.CRASH_ERROR,
                        color = Color(0xFFFF5252),
                        onClick = { selectedFilter = LogType.CRASH_ERROR }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Log List
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay registros para este filtro",
                            color = SpotifyTextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { logItem ->
                            LogEntryCard(entry = logItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) color else SpotifyCardGrey)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else SpotifyTextWhite
        )
    }
}

@Composable
private fun LogEntryCard(entry: LogEntry) {
    var isExpanded by remember { mutableStateOf(false) }

    val (badgeColor, badgeText) = when (entry.type) {
        LogType.ACTION -> Pair(Color(0xFF2196F3), "ACTION")
        LogType.WARNING -> Pair(Color(0xFFFFC107), "WARN")
        LogType.CRASH_ERROR -> Pair(Color(0xFFFF5252), "CRASH/ERR")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF181818))
            .border(
                width = 1.dp,
                color = if (entry.type == LogType.CRASH_ERROR) Color(0xFFFF5252).copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[${entry.tag}]",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpotifyGreen
                )
            }

            Text(
                text = entry.timestamp,
                fontSize = 10.sp,
                color = SpotifyTextMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = entry.message,
            fontSize = 12.sp,
            color = SpotifyTextWhite,
            fontFamily = FontFamily.Monospace
        )

        if (entry.stackTrace != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpanded) "Ocultar Stack Trace ▲" else "Ver Stack Trace Completo ▼",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF5252)
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = entry.stackTrace,
                        fontSize = 10.sp,
                        color = Color(0xFFFF8A80),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
