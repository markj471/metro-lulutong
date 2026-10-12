package com.subway.app.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subway.app.algorithm.PathResult
import com.subway.app.data.Favorite
import com.subway.app.data.FavoriteManager
import com.subway.app.data.local.entity.*
import com.subway.app.data.repository.MetroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NearbyStation(val station: StationEntity, val distance: Int)

object RecommendedRoutePolicy {
    const val TRANSFER_PENALTY = 2
}

class MetroViewModel(private val repository: MetroRepository) : ViewModel() {

    private val _lines = MutableStateFlow<List<LineEntity>>(emptyList())
    val lines: StateFlow<List<LineEntity>> = _lines.asStateFlow()

    private val _stations = MutableStateFlow<List<StationEntity>>(emptyList())
    val stations: StateFlow<List<StationEntity>> = _stations.asStateFlow()

    private val _searchResults = MutableStateFlow<List<StationEntity>>(emptyList())
    val searchResults: StateFlow<List<StationEntity>> = _searchResults.asStateFlow()

    private val _histories = MutableStateFlow<List<HistoryEntity>>(emptyList())
    val histories: StateFlow<List<HistoryEntity>> = _histories.asStateFlow()

    private val _selectedLine = MutableStateFlow<LineEntity?>(null)
    val selectedLine: StateFlow<LineEntity?> = _selectedLine.asStateFlow()

    private val _lineStations = MutableStateFlow<List<StationEntity>>(emptyList())
    val lineStations: StateFlow<List<StationEntity>> = _lineStations.asStateFlow()

    private val _selectedStation = MutableStateFlow<StationEntity?>(null)
    val selectedStation: StateFlow<StationEntity?> = _selectedStation.asStateFlow()

    private val _startStation = MutableStateFlow<StationEntity?>(null)
    val startStation: StateFlow<StationEntity?> = _startStation.asStateFlow()

    private val _endStation = MutableStateFlow<StationEntity?>(null)
    val endStation: StateFlow<StationEntity?> = _endStation.asStateFlow()

    private val _pathResult = MutableStateFlow<PathResult?>(null)
    val pathResult: StateFlow<PathResult?> = _pathResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _nearbyStations = MutableStateFlow<List<NearbyStation>>(emptyList())
    val nearbyStations: StateFlow<List<NearbyStation>> = _nearbyStations.asStateFlow()

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()

    private val _recommendedStation = MutableStateFlow<StationEntity?>(null)
    val recommendedStation: StateFlow<StationEntity?> = _recommendedStation.asStateFlow()

    private var initialized = false
    private lateinit var favoriteManager: FavoriteManager

    fun initialize(context: Context) {
        if (initialized) return; initialized = true
        favoriteManager = FavoriteManager(context)
        _favorites.value = favoriteManager.getAll()

        viewModelScope.launch { repository.getAllLines().collect { _lines.value = it } }
        viewModelScope.launch { repository.getAllStations().collect { _stations.value = it } }
        viewModelScope.launch { repository.getAllHistories().collect { _histories.value = it } }

        viewModelScope.launch {
            repository.getAllHistories().collect { h ->
                if (h.isNotEmpty()) {
                    val top = h.groupBy { it.endStationName }.maxByOrNull { it.value.size }?.key
                    if (top != null) {
                        _recommendedStation.value = _stations.value.find { it.stationName == top }
                    }
                }
            }
        }
    }

    fun selectLine(line: LineEntity) {
        _selectedLine.value = line
        _lineStations.value = repository.getStationsByLine(line.lineId)
    }
    fun clearSelectedLine() { _selectedLine.value = null; _lineStations.value = emptyList() }
    fun searchStations(kw: String) { _searchResults.value = if (kw.isBlank()) emptyList() else repository.searchStations(kw) }
    fun selectStation(s: StationEntity) { _selectedStation.value = s }
    fun clearSelectedStation() { _selectedStation.value = null }

    fun setStartStation(s: StationEntity) { _startStation.value = s; _pathResult.value = null; _errorMessage.value = null }
    fun setEndStation(s: StationEntity) { _endStation.value = s; _pathResult.value = null; _errorMessage.value = null }
    fun clearStartStation() { _startStation.value = null; _pathResult.value = null }
    fun clearEndStation() { _endStation.value = null; _pathResult.value = null }

    fun findPath() {
        val s = _startStation.value; val e = _endStation.value
        if (s == null || e == null) { _errorMessage.value = "\u8BF7\u9009\u62E9\u8D77\u70B9\u548C\u7EC8\u70B9"; return }
        if (s.stationId == e.stationId) { _errorMessage.value = "\u8D77\u7EC8\u70B9\u4E0D\u80FD\u76F8\u540C"; return }
        _pathResult.value = null; _errorMessage.value = null
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.graph.dijkstraWithPenalty(
                    s.stationId,
                    e.stationId,
                    RecommendedRoutePolicy.TRANSFER_PENALTY
                )
                if (result != null) {
                    val distanceKm = computePathDistanceKm(result.path)
                    val finalFare = com.subway.app.algorithm.MetroGraph.calculateFareFromKm(distanceKm)
                    _pathResult.value = result.copy(fare = finalFare, distanceKm = distanceKm)
                    val sm = "\u5171" + result.path.size.toString() + "\u7AD9 " + result.transferCount.toString() + "\u6B21\u6362\u4E58 \u7EA6" + result.totalWeight.toString() + "\u5206 " + finalFare.toString() + "\u5143"
                    repository.insertHistory(HistoryEntity(startStationName = s.stationName, endStationName = e.stationName, resultSummary = sm))
                } else {
                    _errorMessage.value = "\u672A\u627E\u5230\u53EF\u884C\u8DEF\u5F84"
                }
            } catch (ex: Exception) {
                _errorMessage.value = "\u67E5\u8BE2\u5931\u8D25: " + ex.message
            } finally { _isLoading.value = false }
        }
    }

    fun clearPathResult() { _pathResult.value = null; _startStation.value = null; _endStation.value = null }

    fun deleteHistory(id: Int) { viewModelScope.launch { repository.deleteHistory(id) } }
    fun clearAllHistories() { viewModelScope.launch { repository.clearAllHistories() } }
    fun useHistory(start: String, end: String) {
        viewModelScope.launch {
            val s = _stations.value.find { it.stationName == start }
            val e = _stations.value.find { it.stationName == end }
            if (s != null && e != null) { _startStation.value = s; _endStation.value = e; findPath() }
        }
    }

    fun addFavorite(label: String, stationName: String) {
        favoriteManager.add(Favorite(label, stationName))
        _favorites.value = favoriteManager.getAll()
    }
    fun removeFavorite(stationName: String) {
        favoriteManager.remove(stationName)
        _favorites.value = favoriteManager.getAll()
    }

    fun updateNearbyStations(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.Default) {
            val sts = _stations.value; if (sts.isEmpty()) return@launch
            val r = FloatArray(1)
            val nb = sts.mapNotNull { st ->
                val la = st.latitude ?: return@mapNotNull null
                val lo = st.longitude ?: return@mapNotNull null
                Location.distanceBetween(lat, lng, la, lo, r)
                NearbyStation(st, r[0].toInt())
            }.sortedBy { it.distance }.take(8)
            _nearbyStations.value = nb
        }
    }


    // ===== Distance-based fare helpers =====

    /**
     * Compute fare from actual station coordinate distances along the path.
     * Uses haversine formula to sum up km between consecutive stations.
     */
    private fun computePathDistanceKm(path: List<Int>): Double {
        if (path.size < 2) return 0.0
        val stationMap = _stations.value.associateBy { it.stationId }
        var totalKm = 0.0
        for (i in 0 until path.size - 1) {
            val s1 = stationMap[path[i]] ?: continue
            val s2 = stationMap[path[i + 1]] ?: continue
            val lat1 = s1.latitude ?: continue
            val lng1 = s1.longitude ?: continue
            val lat2 = s2.latitude ?: continue
            val lng2 = s2.longitude ?: continue
            totalKm += haversineKm(lat1, lng1, lat2, lng2)
        }
        return totalKm
    }

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = kotlin.math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLng / 2).let { it * it }
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}
