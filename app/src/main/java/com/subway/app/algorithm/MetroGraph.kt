package com.subway.app.algorithm

import java.util.PriorityQueue

/**
 * Edge in the metro graph (connection between two stations).
 * @param toNode destination station ID
 * @param weight travel time in minutes (pure, no penalty)
 * @param lineId the line this edge belongs to
 */
data class Edge(
    val toNode: Int,
    val weight: Int,
    val lineId: Int
)

/**
 * A segment of the computed path.
 */
data class PathSegment(val fromStationId: Int, val toStationId: Int, val lineId: Int, val weight: Int)

/**
 * Result of pathfinding.
 * @param path ordered list of station IDs from start to end
 * @param totalWeight pure travel time in minutes (NO transfer penalty included)
 * @param transferCount number of line changes
 * @param lines unique lines used along the path
 * @param fare ticket price in RMB (calculated from pure travel time)
 * @param transferStationIds station IDs where transfers occur
 * @param segments individual travel segments with line and weight
 */
data class PathResult(
    val path: List<Int>,
    val totalWeight: Int,
    val transferCount: Int,
    val lines: List<Int>,
    val fare: Int = 0,
    val transferStationIds: List<Int> = emptyList(),
    val segments: List<PathSegment> = emptyList(),
    val distanceKm: Double = 0.0
)

/**
 * Metro network graph using Dijkstra's algorithm for shortest path.
 *
 * DESIGN NOTE: penalty is used ONLY for path selection in Dijkstra (to bias
 * toward fewer transfers), but totalWeight/fare in the returned PathResult
 * are computed from rawDist (pure travel time without penalty).
 */
class MetroGraph {

    /** Adjacency list: stationId -> list of outgoing edges */
    private val adjacencyList = mutableMapOf<Int, MutableList<Edge>>()

    /** Default transfer penalty in minutes (walking + waiting time at transfer station) */
    private val TRANSFER_PENALTY = 10

    fun addEdge(from: Int, to: Int, weight: Int, lineId: Int) {
        adjacencyList.computeIfAbsent(from) { mutableListOf() }
            .add(Edge(to, weight, lineId))
        adjacencyList.computeIfAbsent(to) { mutableListOf() }
            .add(Edge(from, weight, lineId))
    }

    fun clear() {
        adjacencyList.clear()
    }

    /**
     * Line-aware Dijkstra with DEFAULT transfer penalty (10 min).
     * Uses the shared implementation with penalty=10.
     */
    fun dijkstra(start: Int, end: Int): PathResult? {
        return dijkstraWithPenalty(start, end, TRANSFER_PENALTY)
    }

    /**
     * Dijkstra with CONFIGURABLE transfer penalty.
     *
     * KEY DESIGN: two distance arrays are maintained:
     *   dist[node]    = travel time + transfer penalty (used for Dijkstra priority)
     *   rawDist[node] = pure travel time               (used for display + fare)
     *
     * This means:
     *   - SHORTEST_TIME (penalty=10):   lightly penalizes transfers
     *   - LEAST_TRANSFERS (penalty=100): heavily penalizes transfers
     *   - LOWEST_FARE (penalty=0):       no penalty, pure shortest travel time
     *
     * In ALL cases, the returned totalWeight and fare are based on rawDist
     * (pure travel time), NOT the penalty-inflated distance.
     *
     * @param start   source station ID
     * @param end     destination station ID
     * @param penalty transfer penalty in minutes (only affects path selection)
     * @return PathResult with raw-time totalWeight and fare, or null if unreachable
     */
    fun dijkstraWithPenalty(start: Int, end: Int, penalty: Int): PathResult? {
        if (start == end) {
            return PathResult(listOf(start), 0, 0, listOf(), 0, emptyList(), emptyList())
        }
        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) {
            return null
        }

        data class RouteState(val stationId: Int, val lineId: Int?)

        val startState = RouteState(start, null)
        val dist = mutableMapOf(startState to 0)
        val rawDist = mutableMapOf(startState to 0)
        val prev = mutableMapOf<RouteState, RouteState>()
        val prevEdge = mutableMapOf<RouteState, Edge>()
        val pq = PriorityQueue<Pair<RouteState, Int>>(compareBy { it.second })
        pq.add(startState to 0)
        var endState: RouteState? = null

        while (pq.isNotEmpty()) {
            val (state, currentDist) = pq.poll() ?: break
            if (currentDist != dist[state]) continue
            if (state.stationId == end) {
                endState = state
                break
            }

            adjacencyList[state.stationId]?.forEach { edge ->
                val transferPenalty = if (state.lineId != null && state.lineId != edge.lineId) penalty else 0
                val nextState = RouteState(edge.toNode, edge.lineId)
                val newDist = currentDist + edge.weight + transferPenalty
                val newRawDist = rawDist.getValue(state) + edge.weight

                if (newDist < dist.getOrDefault(nextState, Int.MAX_VALUE)) {
                    dist[nextState] = newDist
                    rawDist[nextState] = newRawDist
                    prev[nextState] = state
                    prevEdge[nextState] = edge
                    pq.add(nextState to newDist)
                }
            }
        }

        val resolvedEndState = endState ?: return null
        val states = mutableListOf<RouteState>()
        var current: RouteState? = resolvedEndState
        while (current != null) {
            states.add(current)
            current = prev[current]
        }
        states.reverse()
        val path = states.map { it.stationId }

        // Count transfers and build segments
        var transferCount = 0
        val lines = mutableListOf<Int>()
        val transferStationIds = mutableListOf<Int>()
        val segments = mutableListOf<PathSegment>()
        var currentLine: Int? = null

        for (i in 1 until states.size) {
            val state = states[i]
            val edge = prevEdge[state]
            if (edge != null) {
                val fromStation = states[i - 1].stationId
                segments.add(PathSegment(fromStation, state.stationId, edge.lineId, edge.weight))

                if (currentLine == null) {
                    currentLine = edge.lineId
                    lines.add(edge.lineId)
                } else if (edge.lineId != currentLine) {
                    transferCount++
                    // The transfer happens at the from-station (where we switch lines)
                    transferStationIds.add(fromStation)
                    currentLine = edge.lineId
                    if (edge.lineId !in lines) lines.add(edge.lineId)
                }
            }
        }

        // CRITICAL: use rawDist (pure travel time) for totalWeight and fare
        val pureTravelTime = rawDist.getValue(resolvedEndState)
        val fare = calculateFare(pureTravelTime)

        return PathResult(
            path = path,
            totalWeight = pureTravelTime,
            transferCount = transferCount,
            lines = lines,
            fare = fare,
            transferStationIds = transferStationIds,
            segments = segments
        )
    }

    companion object {
        /**
         * Wuhan metro distance-based fare (official pricing).
         * 0-4 km: 2 RMB, 4-8 km: 3, 8-12 km: 4, 12-18 km: 5,
         * 18-24 km: 6, 24-32 km: 7, 32-40 km: 8, 40-50 km: 9, 50+: 10
         *
         * @param timeMinutes pure travel time in minutes (NO penalty)
         * @return fare in RMB
         */
        fun calculateFare(timeMinutes: Int): Int {
            val estimatedKm = timeMinutes * 35.0 / 60.0
            return fareFromKm(estimatedKm)
        }

        /**
         * Calculate fare from actual distance in kilometers.
         * Uses the same official Wuhan metro fare table.
         */
        fun calculateFareFromKm(totalKm: Double): Int {
            return fareFromKm(totalKm)
        }

        private fun fareFromKm(km: Double): Int {
            return when {
                km <= 4 -> 2
                km <= 8 -> 3
                km <= 12 -> 4
                km <= 18 -> 5
                km <= 24 -> 6
                km <= 32 -> 7
                km <= 40 -> 8
                km <= 50 -> 9
                else -> 10
            }
        }
    }
}
