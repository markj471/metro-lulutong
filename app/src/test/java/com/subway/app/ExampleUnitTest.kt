package com.subway.app

import com.subway.app.algorithm.MetroGraph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun routePlannerReportsTransferStationAndRawTravelTime() {
        val graph = MetroGraph()
        graph.addEdge(from = 1, to = 2, weight = 5, lineId = 1)
        graph.addEdge(from = 2, to = 3, weight = 6, lineId = 2)

        val result = graph.dijkstraWithPenalty(start = 1, end = 3, penalty = 10)

        assertNotNull(result)
        assertEquals(listOf(1, 2, 3), result!!.path)
        assertEquals(11, result.totalWeight)
        assertEquals(1, result.transferCount)
        assertEquals(listOf(2), result.transferStationIds)
        assertEquals(listOf(1, 2), result.lines)
    }

    @Test
    fun fareUsesWuhanDistanceBrackets() {
        assertEquals(2, MetroGraph.calculateFareFromKm(4.0))
        assertEquals(3, MetroGraph.calculateFareFromKm(4.1))
        assertEquals(7, MetroGraph.calculateFareFromKm(24.1))
        assertEquals(10, MetroGraph.calculateFareFromKm(50.1))
    }

    @Test
    fun routePlannerKeepsSeparateArrivalLineStatesAtTransferStation() {
        val graph = MetroGraph()
        graph.addEdge(from = 1, to = 2, weight = 1, lineId = 1)
        graph.addEdge(from = 2, to = 3, weight = 1, lineId = 1)
        graph.addEdge(from = 1, to = 4, weight = 1, lineId = 2)
        graph.addEdge(from = 4, to = 3, weight = 2, lineId = 2)
        graph.addEdge(from = 3, to = 5, weight = 1, lineId = 2)

        val result = graph.dijkstraWithPenalty(start = 1, end = 5, penalty = 2)

        assertNotNull(result)
        assertEquals(listOf(1, 4, 3, 5), result!!.path)
        assertEquals(4, result.totalWeight)
        assertEquals(0, result.transferCount)
    }
}
