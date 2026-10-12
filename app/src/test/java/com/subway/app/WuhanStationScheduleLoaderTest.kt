package com.subway.app

import com.subway.app.data.WuhanStationScheduleLoader
import org.junit.Assert.assertEquals
import org.junit.Test

class WuhanStationScheduleLoaderTest {

    @Test
    fun parserKeepsDirectionAndServiceDaySchedules() {
        val json = """
            [{
              "stationName": "天河机场",
              "schedules": [
                {"lineName":"2号线", "direction":"佛祖岭方向", "serviceDay":"工作日", "firstTrain":"06:00", "lastTrain":"23:00"},
                {"lineName":"2号线", "direction":"佛祖岭方向", "serviceDay":"节假日", "firstTrain":"06:30", "lastTrain":"23:00"}
              ]
            }]
        """.trimIndent()

        val schedules = WuhanStationScheduleLoader.parse(json).getValue("天河机场")

        assertEquals(2, schedules.size)
        assertEquals("2号线", schedules.first().lineName)
        assertEquals("佛祖岭方向", schedules.first().direction)
        assertEquals("工作日", schedules.first().serviceDay)
        assertEquals("06:00", schedules.first().firstTrain)
        assertEquals("23:00", schedules.first().lastTrain)
    }
}
