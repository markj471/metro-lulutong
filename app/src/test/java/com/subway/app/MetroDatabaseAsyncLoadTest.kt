package com.subway.app

import com.subway.app.data.WuhanMetroJsonLoader
import com.subway.app.data.local.MetroDatabase
import com.subway.app.data.local.entity.LineEntity
import com.subway.app.data.local.entity.StationEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MetroDatabaseAsyncLoadTest {

    @Test
    fun stationDaoCreatedBeforeAsyncLoadSeesUpdatedLineOrder() {
        val constructor = MetroDatabase::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        val database = constructor.newInstance()
        val stationDao = database.stationDao()
        val data = WuhanMetroJsonLoader.MetroData(
            lines = listOf(LineEntity(2, "2号线", "#FDA6D4")),
            stations = listOf(
                StationEntity(10, "天河机场", "2"),
                StationEntity(11, "佛祖岭", "2")
            ),
            connections = emptyList(),
            lineSequences = mapOf(2 to listOf(10, 11)),
            linePaths = emptyMap()
        )

        database.loadFromMetroData(data)

        assertEquals(listOf("天河机场", "佛祖岭"), stationDao.getStationsByLine(2).map { it.stationName })
    }
}
