package com.subway.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.subway.app.data.local.entity.*
import kotlin.math.*

/**
 * Parses wuhan_metro.json and produces entity lists for the in-memory MetroDatabase.
 * Also extracts line path coordinates for future map rendering.
 */
object WuhanMetroJsonLoader {

    data class LinePathPoint(val x: Float, val y: Float)
    data class LinePath(val lineId: Int, val points: List<LinePathPoint>)

    data class MetroData(
        val lines: List<LineEntity>,
        val stations: List<StationEntity>,
        val connections: List<ConnectionEntity>,
        val lineSequences: Map<Int, List<Int>>,      // lineId -> ordered station IDs
        val linePaths: Map<Int, List<LinePathPoint>>  // lineId -> map drawing points
    )

    fun loadFromAssets(context: Context): MetroData {
        val json = context.assets.open("wuhan_metro.json").bufferedReader().readText()
        val schedules = WuhanStationScheduleLoader.loadFromAssets(context)
        val data = parse(json)
        return data.copy(
            stations = data.stations.map { station ->
                val stationSchedules = schedules[station.stationName].orEmpty()
                station.copy(
                    firstTrain = stationSchedules.firstOrNull()?.firstTrain.orEmpty(),
                    lastTrain = stationSchedules.firstOrNull()?.lastTrain.orEmpty(),
                    schedules = stationSchedules
                )
            }
        )
    }

    fun parse(json: String): MetroData {
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val raw: Map<String, Any> = gson.fromJson(json, mapType)

        @Suppress("UNCHECKED_CAST")
        val linesData = raw["l"] as? List<Map<String, Any>> ?: return emptyData()

        val allLines = mutableListOf<LineEntity>()
        val allStations = mutableListOf<StationEntity>()
        val allConnections = mutableListOf<ConnectionEntity>()
        val lineSeqMap = mutableMapOf<Int, MutableList<Int>>()
        val linePathMap = mutableMapOf<Int, MutableList<LinePathPoint>>()
        val amapIdToLocal = mutableMapOf<String, Int>()
        var nextLocalId = 1
        var connId = 0

        for ((lineIdx, lineData) in linesData.withIndex()) {
            val ln = lineData["ln"] as? String ?: continue
            val cl = lineData["cl"] as? String ?: "888888"
            val lineId = ln.replace(Regex("[^0-9]"), "").toIntOrNull() ?: (100 + lineIdx)
            val colorCode = MetroLinePresentation.displayColorCode(ln, cl)

            allLines.add(LineEntity(lineId, ln, colorCode))

            @Suppress("UNCHECKED_CAST")
            val stationsData = lineData["st"] as? List<Map<String, Any>> ?: continue
            val sourceSeq = mutableListOf<Int>()

            for (stData in stationsData) {
                val si = stData["si"] as? String ?: continue
                val name = stData["n"] as? String ?: ""
                val englishName = StationEnglishNames.forStation(name, stData["sp"] as? String ?: "")
                val sl = stData["sl"] as? String ?: ""
                val t = stData["t"] as? String ?: "0"
                val parts = sl.split(",")
                val lng = parts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 0.0
                val lat = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0

                val localId: Int
                if (si in amapIdToLocal) {
                    localId = amapIdToLocal[si]!!
                } else {
                    localId = nextLocalId++
                    amapIdToLocal[si] = localId
                    allStations.add(StationEntity(
                        stationId = localId,
                        stationName = name,
                        stationEnglishName = englishName,
                        lineIds = "",
                        latitude = lat,
                        longitude = lng,
                        isTransfer = t == "1"
                    ))
                }
                sourceSeq.add(localId)
            }

            val seq = MetroLinePresentation.orderStations(ln, sourceSeq) { stationId ->
                allStations.first { it.stationId == stationId }.stationName
            }.toMutableList()
            lineSeqMap[lineId] = seq

            // Build connections for this line
            for (i in 0 until seq.size - 1) {
                val a = seq[i]; val b = seq[i + 1]
                val sa = allStations.find { it.stationId == a }
                val sb = allStations.find { it.stationId == b }
                val km = if (sa != null && sb != null)
                    haversineKm(sa.latitude!!, sa.longitude!!, sb.latitude!!, sb.longitude!!) else 1.0
                val w = maxOf(2, (km * 60 / 35 + 1).toInt())
                allConnections.add(ConnectionEntity(connId++, a, b, w, lineId))
                allConnections.add(ConnectionEntity(connId++, b, a, w, lineId))
            }

            // Extract line path coordinates
            @Suppress("UNCHECKED_CAST")
            val pathSegments = lineData["f"] as? List<Map<String, Any>>
            val pathPoints = mutableListOf<LinePathPoint>()
            if (pathSegments != null) {
                for (seg in pathSegments) {
                    @Suppress("UNCHECKED_CAST")
                    val points = seg["c"] as? List<String> ?: continue
                    for (pt in points) {
                        val xy = pt.split(" ")
                        val x = xy.getOrNull(0)?.toFloatOrNull() ?: continue
                        val y = xy.getOrNull(1)?.toFloatOrNull() ?: continue
                        pathPoints.add(LinePathPoint(x, y))
                    }
                }
            }
            linePathMap[lineId] = pathPoints
        }

        // Update lineIds for stations appearing on multiple lines
        val stationLineMap = mutableMapOf<Int, MutableSet<Int>>()
        for ((lid, seq) in lineSeqMap) {
            for (sid in seq) {
                stationLineMap.getOrPut(sid) { mutableSetOf() }.add(lid)
            }
        }
        val updatedStations = allStations.map { st ->
            val lids = stationLineMap[st.stationId] ?: emptySet()
            st.copy(
                lineIds = lids.sorted().joinToString(","),
                isTransfer = lids.size > 1
            )
        }

        return MetroData(
            lines = allLines,
            stations = updatedStations,
            connections = allConnections,
            lineSequences = lineSeqMap.mapValues { it.value.toList() },
            linePaths = linePathMap.mapValues { it.value.toList() }
        )
    }

    private fun emptyData() = MetroData(emptyList(), emptyList(), emptyList(), emptyMap(), emptyMap())

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

}
