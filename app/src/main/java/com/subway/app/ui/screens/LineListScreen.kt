package com.subway.app.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.data.MetroLinePresentation
import com.subway.app.data.local.entity.LineEntity
import com.subway.app.data.local.entity.StationEntity
import com.subway.app.viewmodel.MetroViewModel

@Composable
fun LineListScreen(onLineClick: (Int) -> Unit, viewModel: MetroViewModel) {
    val lines by viewModel.lines.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { LineTemplateHeader() }
        if (lines.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        items(lines.chunked(2)) { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                pair.forEach { line ->
                    LineTemplateCard(line, Modifier.weight(1f), onLineClick)
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LineTemplateHeader() {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = CircleShape, color = Color(0xFF4D5BE0), shadowElevation = 2.dp) {
            Icon(Icons.Filled.Train, contentDescription = "武汉地铁", modifier = Modifier.size(82.dp).padding(18.dp), tint = Color.White)
        }
        Text("武汉地铁", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoPill("554 公里")
            InfoPill("335 座车站")
            InfoPill("13 条线路")
        }
        InfoPill("2004年7月 通车")
    }
}

@Composable
private fun InfoPill(text: String) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, border = ButtonDefaults.outlinedButtonBorder, shadowElevation = 1.dp) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LineTemplateCard(line: LineEntity, modifier: Modifier, onLineClick: (Int) -> Unit) {
    val lineColor = line.toLineColor()
    Surface(
        onClick = { onLineClick(line.lineId) },
        modifier = modifier.height(222.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.size(116.dp).clip(CircleShape).border(14.dp, lineColor, CircleShape), contentAlignment = Alignment.Center) {
                Text(line.badgeText(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(line.lineName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(line.englishLineName(), fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LineDetailScreen(
    lineId: Int,
    onStationClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: MetroViewModel
) {
    val allLines by viewModel.lines.collectAsState()
    val lineStations by viewModel.lineStations.collectAsState()
    val selectedLine by viewModel.selectedLine.collectAsState()

    LaunchedEffect(lineId, allLines) {
        allLines.find { it.lineId == lineId }?.let(viewModel::selectLine)
    }

    val line = selectedLine ?: allLines.find { it.lineId == lineId }
    val lineColor = line?.toLineColor() ?: MaterialTheme.colorScheme.primary
    val headerContentColor = line?.toLineContentColor() ?: Color.White
    val totalDuration = ((lineStations.size - 1).coerceAtLeast(0)) * 3
    val startStation = lineStations.firstOrNull()?.stationName.orEmpty()
    val endStation = lineStations.lastOrNull()?.stationName.orEmpty()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = lineColor, shadowElevation = 2.dp) {
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = headerContentColor) }
                    Column {
                        Text(line?.lineName ?: "${lineId}号线", color = headerContentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${lineStations.size}个站点  约${totalDuration}分钟", color = headerContentColor.copy(alpha = 0.75f), fontSize = 13.sp)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    EndpointLabel("起点站", startStation, headerContentColor)
                    EndpointLabel("终点站", endStation, headerContentColor)
                }
            }
        }

        when {
            lineStations.isEmpty() && line != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = lineColor)
            }
            lineStations.isNotEmpty() -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    Text("途经站点（${lineStations.size}站）", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(lineStations.size) { index ->
                    val station = lineStations[index]
                    StationTimelineItem(
                        station = station,
                        isFirst = index == 0,
                        isLast = index == lineStations.lastIndex,
                        lineColor = lineColor,
                        onClick = { onStationClick(station.stationId) }
                    )
                }
            }
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无站点数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EndpointLabel(label: String, stationName: String, contentColor: Color) {
    Column {
        Text(label, fontSize = 10.sp, color = contentColor.copy(alpha = 0.7f))
        Text(stationName, fontSize = 13.sp, color = contentColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StationTimelineItem(
    station: StationEntity,
    isFirst: Boolean,
    isLast: Boolean,
    lineColor: Color,
    onClick: () -> Unit
) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
            if (!isFirst) Box(Modifier.width(2.dp).height(8.dp).background(lineColor.copy(alpha = 0.45f)))
            Box(
                Modifier.size(if (station.isTransfer) 14.dp else 10.dp).clip(CircleShape)
                    .background(if (station.isTransfer) MaterialTheme.colorScheme.error else lineColor)
            )
            if (!isLast) Box(Modifier.width(2.dp).height(32.dp).background(lineColor.copy(alpha = 0.45f)))
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (station.isTransfer) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
            shadowElevation = if (station.isTransfer) 1.dp else 0.dp,
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, bottom = 6.dp)
        ) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        station.stationName,
                        fontSize = 15.sp,
                        fontWeight = if (station.isTransfer || isFirst || isLast) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (station.isTransfer) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    if (station.stationEnglishName.isNotBlank()) {
                        Text(station.stationEnglishName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (station.isTransfer) Text("换乘站", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isFirst) EndpointBadge("起点", lineColor)
                    if (isLast) EndpointBadge("终点", lineColor)
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EndpointBadge(text: String, lineColor: Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = lineColor.copy(alpha = 0.14f)) {
        Text(text, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = lineColor)
    }
}

private fun LineEntity.toLineColor(): Color = try {
    Color(AndroidColor.parseColor(colorCode))
} catch (_: Exception) {
    Color.Gray
}

private fun LineEntity.badgeText(): String = when (lineName) {
    "阳逻线" -> "YL"
    else -> lineName.replace("号线", "")
}

private fun LineEntity.englishLineName(): String = when (lineName) {
    "阳逻线" -> "Line 21"
    else -> "Line ${lineName.filter { it.isDigit() }}"
}

private fun LineEntity.toLineContentColor(): Color = Color(
    AndroidColor.parseColor(MetroLinePresentation.foregroundColorCode(lineName))
)
