package com.subway.app.data.repository

import com.subway.app.algorithm.MetroGraph
import com.subway.app.algorithm.PathResult
import com.subway.app.data.local.*
import com.subway.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class MetroRepository(
    private val lineDao: LineDao,
    private val stationDao: StationDao,
    private val connectionDao: ConnectionDao,
    private val historyDao: HistoryDao,
    val database: MetroDatabase
) {
    val graph: MetroGraph get() = database.graph

    fun getAllLines(): Flow<List<LineEntity>> = lineDao.getAllLines()
    fun getLineById(lineId: Int): LineEntity? = lineDao.getLineById(lineId)
    suspend fun insertLines(lines: List<LineEntity>) { lineDao.insertLines(lines) }

    fun getAllStations(): Flow<List<StationEntity>> = stationDao.getAllStations()
    fun getStationById(stationId: Int): StationEntity? = stationDao.getStationById(stationId)
    fun searchStations(keyword: String): List<StationEntity> = stationDao.searchStations(keyword)
    fun getStationsByLine(lineId: Int): List<StationEntity> = stationDao.getStationsByLine(lineId)
    suspend fun insertStations(stations: List<StationEntity>) { stationDao.insertStations(stations) }

    suspend fun insertConnections(connections: List<ConnectionEntity>) { connectionDao.insertConnections(connections) }
    fun getAllConnections(): Flow<List<ConnectionEntity>> = connectionDao.getAllConnections()

    fun getAllHistories(): Flow<List<HistoryEntity>> = historyDao.getAllHistories()
    suspend fun insertHistory(history: HistoryEntity) { historyDao.insertHistory(history) }
    suspend fun deleteHistory(historyId: Int) { historyDao.deleteHistory(historyId) }
    suspend fun clearAllHistories() { historyDao.clearAll() }

    fun findShortestPath(startStationId: Int, endStationId: Int): PathResult? {
        return graph.dijkstra(startStationId, endStationId)
    }
}
