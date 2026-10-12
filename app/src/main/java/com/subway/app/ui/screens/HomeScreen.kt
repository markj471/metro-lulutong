package com.subway.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.R
import com.subway.app.data.MetroLinePresentation
import com.subway.app.data.local.entity.StationEntity
import com.subway.app.ui.map.MapOffset
import com.subway.app.ui.map.MapViewportPolicy
import com.subway.app.viewmodel.MetroViewModel

private const val MAP_ASPECT_RATIO = 4300f / 3425f

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToLines: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onNearbyStationClick: (StationEntity) -> Unit,
    viewModel: MetroViewModel
) {
    val lines by viewModel.lines.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val nearbyStations by viewModel.nearbyStations.collectAsState()
    val recommendedStation by viewModel.recommendedStation.collectAsState()
    val publishedStationCount = remember(stations) {
        MetroLinePresentation.countLineStations(stations.map { it.lineIds })
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                "武汉地铁路路通",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "${lines.size}条线路  ${publishedStationCount}个站点",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ZoomableMetroMap(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp))

        recommendedStation?.let { station ->
            Surface(
                onClick = { viewModel.setStartStation(station); onNavigateToRoute() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lightbulb, null, Modifier.size(22.dp), tint = Color(0xFFF39800))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("猜你想去", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(station.stationName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("去规划", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (nearbyStations.isNotEmpty()) {
            LazyRow(
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nearbyStations.take(6)) { nearby ->
                    Surface(
                        onClick = { onNearbyStationClick(nearby.station) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, Modifier.size(14.dp), tint = Color(0xFF34C759))
                            Spacer(Modifier.width(4.dp))
                            Text(nearby.station.stationName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(4.dp))
                            Text("${nearby.distance}m", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickAction("换乘规划", Icons.Filled.Route, MaterialTheme.colorScheme.primary, Modifier.weight(1f), onNavigateToRoute)
            QuickAction("全部线路", Icons.Filled.Train, Color(0xFFF39800), Modifier.weight(1f), onNavigateToLines)
            QuickAction("搜索站点", Icons.Filled.Search, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f), onNavigateToSearch)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp, vertical = 2.dp).clickable(onClick = onNavigateToSearch),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("搜索站点、线路", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ZoomableMetroMap(modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var mapOffset by remember { mutableStateOf(MapOffset(0f, 0f)) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    fun reset() {
        scale = 1f
        mapOffset = MapOffset(0f, 0f)
    }

    fun clampOffset(requestedX: Float, requestedY: Float, requestedScale: Float = scale): MapOffset {
        val width = viewportSize.width.toFloat()
        val contentHeight = if (width > 0f) width / MAP_ASPECT_RATIO else 0f
        return MapViewportPolicy.clampOffset(
            viewportWidth = width,
            viewportHeight = viewportSize.height.toFloat(),
            contentWidth = width,
            contentHeight = contentHeight,
            scale = requestedScale,
            requestedX = requestedX,
            requestedY = requestedY
        )
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = MapViewportPolicy.clampScale(scale * zoomChange)
        mapOffset = clampOffset(mapOffset.x + panChange.x, mapOffset.y + panChange.y, newScale)
        scale = newScale
    }

    LaunchedEffect(viewportSize) {
        mapOffset = clampOffset(mapOffset.x, mapOffset.y)
    }

    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .onSizeChanged { viewportSize = it },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.wuhan_metro_map),
            contentDescription = "武汉地铁线路图，可双指缩放",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MAP_ASPECT_RATIO)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = mapOffset.x
                    translationY = mapOffset.y
                }
                .pointerInput(Unit) { detectTapGestures(onDoubleTap = { reset() }) }
                .transformable(transformState)
        )

        FilledTonalIconButton(
            onClick = ::reset,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Icon(Icons.Filled.RestartAlt, contentDescription = "重置线路图缩放")
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
