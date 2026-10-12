package com.subway.app.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.data.local.entity.LineEntity
import com.subway.app.data.local.entity.StationEntity
import com.subway.app.data.MetroLinePresentation
import com.subway.app.viewmodel.MetroViewModel

@Composable
fun StationDetailScreen(
    stationId: Int,
    onNavigateToLine: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: MetroViewModel
) {
    val allStations by viewModel.stations.collectAsState()
    val allLines by viewModel.lines.collectAsState()
    val station = remember(allStations, stationId) { allStations.find { it.stationId == stationId } }

    if (station == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val st = station!!
    val lineIds = st.lineIds.split(",").mapNotNull { it.trim().toIntOrNull() }
    val lineById = allLines.associateBy { it.lineId }
    val firstLineColor = lineIds.firstOrNull()?.let { lineById[it]?.toComposeColor() }
        ?: MaterialTheme.colorScheme.primary
    val headerContentColor = lineIds.firstOrNull()?.let { lineById[it]?.toComposeContentColor() } ?: Color.White

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(shadowElevation = 2.dp, color = firstLineColor) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = headerContentColor)
                }
                Column {
                    Text(st.stationName, color = headerContentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    if (st.stationEnglishName.isNotBlank()) Text(st.stationEnglishName, color = headerContentColor.copy(alpha = 0.78f), fontSize = 13.sp)
                    if (st.isTransfer) Text("换乘站", color = headerContentColor.copy(alpha = 0.75f), fontSize = 14.sp)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("所属线路", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
            items(lineIds) { lineId ->
                val line = lineById[lineId] ?: return@items
                val lineColor = line.toComposeColor()
                Surface(
                    onClick = { onNavigateToLine(line.lineId) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(lineColor), contentAlignment = Alignment.Center) {
                            Text(line.lineName.replace("号线", ""), color = line.toComposeContentColor(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(line.lineName, Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }

            item {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Text("运营时间", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        if (st.schedules.isEmpty()) {
                            Text("暂无该站点时刻表数据", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        } else {
                            st.schedules.groupBy { it.lineName }.forEach { (lineName, schedules) ->
                                Text(lineName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                schedules.groupBy { it.direction }.forEach { (direction, directionSchedules) ->
                                    Text(direction, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                                    directionSchedules.forEach { schedule ->
                                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(schedule.serviceDay, fontSize = 13.sp)
                                            Text("${schedule.firstTrain} - ${schedule.lastTrain}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                if (lineName != st.schedules.groupBy { it.lineName }.keys.last()) {
                                    HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Info, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("站点信息", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(10.dp))
                        StationInfoRow("站点名称", st.stationName)
                        StationInfoRow("英文名称", st.stationEnglishName.ifBlank { "暂无" })
                        StationInfoRow("所属线路", lineIds.joinToString(" / ") { lineById[it]?.lineName ?: "${it}号线" })
                        StationInfoRow("站点类型", if (st.isTransfer) "换乘站" else "普通站")
                        StationInfoRow("线路数量", "${lineIds.size} 条")
                        StationInfoRow("数据日期", "2026-07-20")
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

private fun LineEntity.toComposeColor(): Color = try {
    Color(AndroidColor.parseColor(colorCode))
} catch (_: Exception) {
    Color.Gray
}

private fun LineEntity.toComposeContentColor(): Color = Color(
    AndroidColor.parseColor(MetroLinePresentation.foregroundColorCode(lineName))
)

@Composable
private fun StationInfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
