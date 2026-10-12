package com.subway.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.data.search.SearchHistoryManager
import com.subway.app.viewmodel.MetroViewModel

@Composable
fun StationSearchScreen(
    onStationClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: MetroViewModel
) {
    val context = LocalContext.current
    val historyManager = remember { SearchHistoryManager(context) }
    var query by remember { mutableStateOf("") }
    var histories by remember { mutableStateOf(historyManager.getHistory()) }
    val results by viewModel.searchResults.collectAsState()
    val hotSearches = remember { listOf("江汉路", "光谷广场", "武汉站", "洪山广场", "街道口", "中南路") }

    fun applyKeyword(keyword: String) {
        query = keyword
        viewModel.searchStations(keyword)
        histories = historyManager.add(keyword)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface) }
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, shadowElevation = 2.dp, modifier = Modifier.weight(1f).heightIn(min = 50.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        TextField(
                            value = query,
                            onValueChange = { q -> query = q; viewModel.searchStations(q) },
                            placeholder = { Text("\u641c\u7d22\u7ad9\u70b9\u3001\u7ebf\u8def") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = ""; viewModel.searchStations("") }) {
                                Icon(Icons.Filled.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (query.isBlank()) {
                item {
                    SearchSuggestionSection(
                        title = "\u641c\u7d22\u5386\u53f2",
                        items = histories,
                        emptyText = "\u6682\u65e0\u641c\u7d22\u5386\u53f2",
                        onClear = {
                            historyManager.clear()
                            histories = emptyList()
                        },
                        onClick = ::applyKeyword
                    )
                }
                item {
                    SearchSuggestionSection(
                        title = "\u70ed\u95e8\u641c\u7d22",
                        items = hotSearches,
                        emptyText = "",
                        onClear = null,
                        onClick = ::applyKeyword
                    )
                }
            }
            items(results) { st ->
                Surface(
                    onClick = {
                        histories = historyManager.add(st.stationName)
                        onStationClick(st.stationId)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(st.stationName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            if (st.stationEnglishName.isNotBlank()) Text(st.stationEnglishName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (st.isTransfer) "\u6362\u4e58\u7ad9" else "\u666e\u901a\u7ad9",
                                fontSize = 12.sp,
                                color = if (st.isTransfer) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (st.isTransfer) {
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.errorContainer) {
                                Text("\u6362\u4e58\u7ad9", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (results.isEmpty() && query.isNotBlank()) {
                item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("\u672a\u627e\u5230\u7ed3\u679c", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
        }
    }
}

@Composable
private fun SearchSuggestionSection(
    title: String,
    items: List<String>,
    emptyText: String,
    onClear: (() -> Unit)?,
    onClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (onClear != null && items.isNotEmpty()) {
                TextButton(onClick = onClear) { Text("\u6e05\u7a7a", fontSize = 12.sp) }
            }
        }
        if (items.isEmpty()) {
            if (emptyText.isNotBlank()) Text(emptyText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { keyword ->
                    Surface(
                        onClick = { onClick(keyword) },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(keyword, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}
