package com.subway.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.data.Favorite
import com.subway.app.data.local.entity.StationEntity
import com.subway.app.data.local.entity.HistoryEntity
import com.subway.app.viewmodel.MetroViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onHistoryClick: (String, String) -> Unit,
    onNearbyStationClick: (StationEntity) -> Unit,
    viewModel: MetroViewModel
) {
    val histories by viewModel.histories.collectAsState()
    val nearbyStations by viewModel.nearbyStations.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val allStations by viewModel.stations.collectAsState()
    var showAbout by remember { mutableStateOf(false) }
    var showAddFavorite by remember { mutableStateOf(false) }
    var favStationName by remember { mutableStateOf("") }
    var favLabel by remember { mutableStateOf("\u5E38\u7528") }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("\u6211\u7684", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        }

        // Favorites section
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("\u6536\u85CF\u5939", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { showAddFavorite = true }) {
                    Icon(Icons.Filled.Add, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("\u6DFB\u52A0", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
        }
        if (favorites.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("\u6682\u65E0\u6536\u85CF\uFF0C\u70B9\u51FB\u53F3\u4E0A\u89D2\u6DFB\u52A0", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(favorites) { fav ->
                val st = allStations.find { it.stationName == fav.stationName }
                Surface(
                    onClick = { if (st != null) onNearbyStationClick(st) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (fav.label) {
                            "\u5BB6" -> Icons.Filled.Home
                            "\u516C\u53F8" -> Icons.Filled.Business
                            "\u5B66\u6821" -> Icons.Filled.School
                            else -> Icons.Filled.Star
                        }
                        val labelColor = when (fav.label) {
                            "\u5BB6" -> Color(0xFFE53935)
                            "\u516C\u53F8" -> Color(0xFF3366FF)
                            "\u5B66\u6821" -> Color(0xFFF39800)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = labelColor.copy(alpha = 0.1f)) {
                            Icon(icon, null, Modifier.size(32.dp).padding(6.dp), tint = labelColor)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(fav.stationName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(fav.label, fontSize = 12.sp, color = labelColor)
                        }
                        TextButton(onClick = {
                            viewModel.removeFavorite(fav.stationName)
                        }) {
                            Text("\u5220\u9664", fontSize = 12.sp, color = Color(0xFFE53935))
                        }
                    }
                }
            }
        }

        // Nearby stations
        if (nearbyStations.isNotEmpty()) {
            item { Text("\u9644\u8FD1\u7AD9\u70B9", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                    Column(Modifier.padding(4.dp)) {
                        nearbyStations.take(6).forEach { ns ->
                            ListItem(
                                headlineContent = { Text(ns.station.stationName, fontSize = 15.sp) },
                                supportingContent = { Text("\u7EA6" + ns.distance.toString() + "m", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                leadingContent = { Icon(Icons.Outlined.LocationOn, null, tint = Color(0xFF34C759)) },
                                trailingContent = {
                                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text("\u8BBE\u4E3A\u8D77\u70B9", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                modifier = Modifier.clickable { onNearbyStationClick(ns.station) }
                            )
                        }
                    }
                }
            }
        }

        // History
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("\u67E5\u8BE2\u5386\u53F2", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (histories.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllHistories() }) {
                        Text("\u6E05\u7A7A\u5168\u90E8", color = Color(0xFFE53935), fontSize = 13.sp)
                    }
                }
            }
        }

        if (histories.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("\u6682\u65E0\u5386\u53F2\u8BB0\u5F55", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(histories) { h ->
                val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                Surface(
                    onClick = { onHistoryClick(h.startStationName, h.endStationName) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(h.startStationName + "  \u2192  " + h.endStationName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(h.resultSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(fmt.format(Date(h.queryTime)), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { viewModel.deleteHistory(h.historyId) }) {
                            Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }

        // Settings
        item {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Column {
                    ProfileRow(Icons.Outlined.Settings, "设置", onSettingsClick)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileRow(Icons.Outlined.Info, "关于武汉地铁路路通") { showAbout = true }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    // Add favorite dialog
    if (showAddFavorite) {
        val labelOptions = listOf("\u5BB6", "\u516C\u53F8", "\u5B66\u6821", "\u5E38\u7528")
        AlertDialog(
            onDismissRequest = { showAddFavorite = false },
            title = { Text("\u6DFB\u52A0\u6536\u85CF\u7AD9\u70B9", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = favStationName,
                        onValueChange = { favStationName = it },
                        label = { Text("\u7AD9\u70B9\u540D\u79F0") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("\u6807\u7B7E", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        labelOptions.forEach { option ->
                            val selected = favLabel == option
                            FilterChip(
                                selected = selected,
                                onClick = { favLabel = option },
                                label = { Text(option, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (favStationName.isNotBlank()) {
                        viewModel.addFavorite(favLabel, favStationName.trim())
                        favStationName = ""
                        showAddFavorite = false
                    }
                }) { Text("\u786E\u5B9A") }
            },
            dismissButton = { TextButton(onClick = { showAddFavorite = false }) { Text("\u53D6\u6D88") } }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { Icon(Icons.Filled.Train, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("武汉地铁路路通", fontWeight = FontWeight.Bold) },
            text = { Text("武汉地铁信息查询系统\n\n版本 1.0\n13 条线路 · 335 个线路站点\n289 座唯一车站\n\n离线数据于 2026-07-20 依据公开武汉地铁信息核对。") },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("\u786E\u5B9A") } }
        )
    }
}

@Composable
fun ProfileRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label, fontSize = 15.sp) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.clickable { onClick() }
    )
}
