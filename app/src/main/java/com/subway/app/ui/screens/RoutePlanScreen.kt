package com.subway.app.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.algorithm.PathResult
import com.subway.app.algorithm.PathSegment
import com.subway.app.data.local.entity.LineEntity
import com.subway.app.data.local.entity.StationEntity
import com.subway.app.viewmodel.MetroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlanScreen(onBack: () -> Unit, viewModel: MetroViewModel) {
    val start by viewModel.startStation.collectAsState()
    val end by viewModel.endStation.collectAsState()
    val result by viewModel.pathResult.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val allStations by viewModel.stations.collectAsState()
    val allLines by viewModel.lines.collectAsState()
    val nearbyStations by viewModel.nearbyStations.collectAsState()
    var showStartSheet by remember { mutableStateOf(false) }
    var showEndSheet by remember { mutableStateOf(false) }
    val lineColors = remember(allLines) { allLines.associate { it.lineId to it.toRouteColor() } }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("换乘规划", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                    Column {
                        RouteStationSelector("起点", start, Color(0xFF34C759)) { showStartSheet = true }
                        HorizontalDivider(Modifier.padding(start = 46.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        RouteStationSelector("终点", end, MaterialTheme.colorScheme.error) { showEndSheet = true }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedIconButton(
                        onClick = {
                            val oldStart = start
                            val oldEnd = end
                            if (oldStart != null && oldEnd != null) {
                                viewModel.setStartStation(oldEnd)
                                viewModel.setEndStation(oldStart)
                            }
                        },
                        enabled = start != null && end != null,
                        modifier = Modifier.size(50.dp)
                    ) { Icon(Icons.Filled.SwapVert, contentDescription = "交换起点终点") }
                    Button(
                        onClick = viewModel::findPath,
                        enabled = start != null && end != null && !loading,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Filled.Route, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("查询路线", fontSize = 15.sp)
                        }
                    }
                }
            }

            if (nearbyStations.isNotEmpty() && (start == null || end == null)) {
                item {
                    Text("附近站点", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(nearbyStations) { nearby ->
                            SuggestionChip(
                                onClick = {
                                    if (start == null) viewModel.setStartStation(nearby.station)
                                    else viewModel.setEndStation(nearby.station)
                                },
                                label = { Text(nearby.station.stationName) },
                                icon = { Icon(Icons.Filled.LocationOn, null, Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            error?.let { message ->
                item {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(message, Modifier.fillMaxWidth().padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            result?.let { route ->
                item { RecommendedRouteSummary(route, allLines, lineColors) }
                items(route.path.size) { index ->
                    val stationId = route.path[index]
                    val station = allStations.find { it.stationId == stationId } ?: return@items
                    val segment = when {
                        index < route.segments.size -> route.segments[index]
                        else -> route.segments.lastOrNull()
                    }
                    val line = segment?.let { item -> allLines.find { it.lineId == item.lineId } }
                    RouteTimelineItem(
                        station = station,
                        isFirst = index == 0,
                        isLast = index == route.path.lastIndex,
                        isTransfer = stationId in route.transferStationIds,
                        lineColor = segment?.let { lineColors[it.lineId] } ?: MaterialTheme.colorScheme.primary,
                        segment = if (index < route.segments.size) segment else null,
                        line = line
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showStartSheet) StationSheet(allLines, allStations, "选择起点") { selected ->
        showStartSheet = false
        selected?.let(viewModel::setStartStation)
    }
    if (showEndSheet) StationSheet(allLines, allStations, "选择终点") { selected ->
        showEndSheet = false
        selected?.let(viewModel::setEndStation)
    }
}

@Composable
private fun RouteStationSelector(label: String, station: StationEntity?, dotColor: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp))
        Text(
            station?.stationName ?: "请选择站点",
            fontSize = 16.sp,
            fontWeight = if (station != null) FontWeight.Medium else FontWeight.Normal,
            color = if (station != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun RecommendedRouteSummary(route: PathResult, lines: List<LineEntity>, lineColors: Map<Int, Color>) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(16.dp)) {
            Text("推荐路线", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RouteStat("${route.totalWeight}分钟", "预计时间")
                RouteStat("¥${route.fare}", "票价")
                RouteStat(if (route.transferCount == 0) "直达" else "${route.transferCount}次", "换乘")
                RouteStat("%.1fkm".format(route.distanceKm), "里程")
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                route.lines.forEach { lineId ->
                    val line = lines.find { it.lineId == lineId }
                    val color = lineColors[lineId] ?: Color.Gray
                    Surface(shape = RoundedCornerShape(5.dp), color = color.copy(alpha = 0.18f)) {
                        Text(line?.lineName ?: "${lineId}号线", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RouteTimelineItem(
    station: StationEntity,
    isFirst: Boolean,
    isLast: Boolean,
    isTransfer: Boolean,
    lineColor: Color,
    segment: PathSegment?,
    line: LineEntity?
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
            if (!isFirst) Box(Modifier.width(3.dp).height(10.dp).background(lineColor.copy(alpha = 0.4f)))
            Box(Modifier.size(if (isTransfer) 16.dp else 12.dp).clip(CircleShape).background(if (isTransfer) MaterialTheme.colorScheme.error else lineColor))
            if (!isLast) Box(Modifier.width(3.dp).height(30.dp).background(lineColor.copy(alpha = 0.4f)))
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isTransfer) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, bottom = 6.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(station.stationName, fontSize = 15.sp, fontWeight = if (isFirst || isLast) FontWeight.Bold else FontWeight.Normal)
                    Text(when { isFirst -> "起点"; isLast -> "终点"; isTransfer -> "换乘"; else -> "" }, fontSize = 10.sp, color = if (isTransfer) MaterialTheme.colorScheme.error else lineColor)
                }
                if (segment != null && line != null) {
                    Text("乘坐 ${line.lineName} · 约${segment.weight}分钟", fontSize = 12.sp, color = lineColor)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSheet(lines: List<LineEntity>, allStations: List<StationEntity>, title: String, onSelected: (StationEntity?) -> Unit) {
    var searchText by remember { mutableStateOf("") }
    var selectedLineId by remember { mutableStateOf<Int?>(null) }
    val lineById = remember(lines) { lines.associateBy { it.lineId } }
    val filteredStations = remember(allStations, searchText, selectedLineId) {
        allStations.filter { station ->
            (searchText.isBlank() || station.stationName.contains(searchText, true)) &&
                (selectedLineId == null || station.lineIds.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(selectedLineId))
        }.sortedBy { it.stationName }
    }

    ModalBottomSheet(
        onDismissRequest = { onSelected(null) },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("搜索站点...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) IconButton(onClick = { searchText = "" }) { Icon(Icons.Filled.Clear, null) }
                }
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lines) { line ->
                    FilterChip(
                        selected = selectedLineId == line.lineId,
                        onClick = { selectedLineId = if (selectedLineId == line.lineId) null else line.lineId },
                        label = { Text(line.lineName, fontSize = 13.sp) }
                    )
                }
            }
            Text("${filteredStations.size} 个站点", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 6.dp))
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                items(filteredStations) { station ->
                    ListItem(
                        headlineContent = { Text(station.stationName, fontWeight = FontWeight.Medium) },
                        supportingContent = {
                            Text(
                                station.lineIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                                    .joinToString(" / ") { lineById[it]?.lineName ?: "${it}号线" },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.clickable { onSelected(station) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun LineEntity.toRouteColor(): Color = try {
    Color(AndroidColor.parseColor(colorCode))
} catch (_: Exception) {
    Color.Gray
}
