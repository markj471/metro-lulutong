package com.subway.app.data.local

import android.content.Context
import com.subway.app.algorithm.MetroGraph
import com.subway.app.data.MetroDataInitializer
import com.subway.app.data.local.entity.ConnectionEntity
import com.subway.app.data.local.entity.HistoryEntity
import com.subway.app.data.local.entity.LineEntity
import com.subway.app.data.local.entity.StationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

class MetroDatabase private constructor() {

    private val _lines = MutableStateFlow<List<LineEntity>>(emptyList())
    private val _stations = MutableStateFlow<List<StationEntity>>(emptyList())
    private val _connections = MutableStateFlow<List<ConnectionEntity>>(emptyList())
    private val _histories = MutableStateFlow<List<HistoryEntity>>(emptyList())

    val graph = MetroGraph()
    private var historyIdCounter = 1
    private var initialized = false

    // stationId -> position index for each line
    val lineOrder: MutableMap<Int, Map<Int, Int>> = ConcurrentHashMap()
    private val defaultLineOrder: Map<Int, Map<Int, Int>> by lazy {
        MetroDataInitializer.lineSequences.mapValues { (_, seq) ->
            seq.mapIndexed { index, id -> id to index }.toMap()
        }
    }


    
    fun isInitialized(): Boolean = initialized


    fun loadFromMetroData(data: com.subway.app.data.WuhanMetroJsonLoader.MetroData) {
        val lo = data.lineSequences.mapValues { (_, seq) ->
            seq.mapIndexed { index, id -> id to index }.toMap()
        }
        lineOrder.clear()
        lineOrder.putAll(lo)
        graph.clear()
        data.connections.forEach { conn ->
            graph.addEdge(conn.fromStationId, conn.toStationId, conn.weight, conn.lineId)
        }
        _connections.value = data.connections
        _stations.value = data.stations
        _lines.value = data.lines
        initialized = true
    }

    fun ensureInitialized() {
        if (initialized) return
        initialized = true
        val lo = MetroDataInitializer.lineSequences.mapValues { (_, seq) ->
            seq.mapIndexed { index, id -> id to index }.toMap()
        }
        lineOrder.putAll(lo)
        val lines = MetroDataInitializer.getInitialLines()
        val stations = MetroDataInitializer.getInitialStations()
        val connections = MetroDataInitializer.getInitialConnections()
        connections.forEach { conn ->
            graph.addEdge(conn.fromStationId, conn.toStationId, conn.weight, conn.lineId)
        }
        _connections.value = connections
        _stations.value = stations
        _lines.value = lines
    }

    fun lineDao() = LineDao(_lines)
    fun stationDao() = StationDao(_stations, lineOrder)
    fun connectionDao() = ConnectionDao(_connections)
    fun historyDao() = HistoryDao(_histories, { historyIdCounter++ })

    companion object {
        @Volatile private var instance: MetroDatabase? = null
        fun getDatabase(context: Context): MetroDatabase {
            return instance ?: synchronized(this) {
                instance ?: MetroDatabase().also { instance = it }
            }
        }
    }
}

class LineDao(private val lines: MutableStateFlow<List<LineEntity>>) {
    fun getAllLines(): Flow<List<LineEntity>> = lines
    fun getLineById(lineId: Int): LineEntity? = lines.value.find { it.lineId == lineId }
    suspend fun insertLines(list: List<LineEntity>) { lines.value = list }
    fun insertLinesNow(list: List<LineEntity>) { lines.value = list }
    fun getAllLinesNow(): List<LineEntity> = lines.value
}

class StationDao(
    private val stations: MutableStateFlow<List<StationEntity>>,
    private val lineOrder: MutableMap<Int, Map<Int, Int>>
) {
    fun getAllStations(): Flow<List<StationEntity>> = stations
    fun getStationById(stationId: Int): StationEntity? = stations.value.find { it.stationId == stationId }
    fun searchStations(keyword: String): List<StationEntity> {
        return stations.value.filter { it.stationName.contains(keyword, true) }
    }
    fun getStationsByLine(lineId: Int): List<StationEntity> {
        val order = lineOrder[lineId] ?: return emptyList()
        return stations.value
            .filter { it.lineIds.split(",").mapNotNull { s -> s.trim().toIntOrNull() }.contains(lineId) }
            .sortedBy { order[it.stationId] ?: Int.MAX_VALUE }
    }
    suspend fun insertStations(list: List<StationEntity>) { stations.value = list }
    fun insertStationsNow(list: List<StationEntity>) { stations.value = list }
    fun getAllStationsNow(): List<StationEntity> = stations.value
}

class ConnectionDao(private val connections: MutableStateFlow<List<ConnectionEntity>>) {
    fun getAllConnections(): Flow<List<ConnectionEntity>> = connections
    fun getAllConnectionsList(): List<ConnectionEntity> = connections.value
    suspend fun insertConnections(list: List<ConnectionEntity>) { connections.value = list }
    fun insertConnectionsNow(list: List<ConnectionEntity>) { connections.value = list }
}

class HistoryDao(
    private val histories: MutableStateFlow<List<HistoryEntity>>,
    private val nextId: () -> Int
) {
    fun getAllHistories(): Flow<List<HistoryEntity>> = histories
    suspend fun insertHistory(history: HistoryEntity) {
        histories.value = listOf(history.copy(historyId = nextId())) + histories.value
    }
    suspend fun deleteHistory(id: Int) {
        histories.value = histories.value.filter { it.historyId != id }
    }
    suspend fun clearAll() {
        histories.value = emptyList()
    }
}
