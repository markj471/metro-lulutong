package com.subway.app

import com.subway.app.data.WuhanMetroJsonLoader
import org.junit.Assert.assertEquals
import org.junit.Test

class WuhanMetroJsonLoaderTest {

    @Test
    fun parserAppliesPublicColorAndDirectionAtDataBoundary() {
        val json = """
            {
              "l": [{
                "ln": "2号线",
                "cl": "EB81B9",
                "st": [
                  {"si":"a", "n":"佛祖岭", "sl":"114.44,30.49", "t":"0"},
                  {"si":"b", "n":"藏龙东街", "sp":"cang long dong jie", "sl":"114.42,30.48", "t":"0"},
                  {"si":"c", "n":"天河机场", "sp":"tian he ji chang", "sl":"114.21,30.77", "t":"0"}
                ],
                "f": []
              }]
            }
        """.trimIndent()

        val data = WuhanMetroJsonLoader.parse(json)
        val stationsById = data.stations.associateBy { it.stationId }
        val orderedNames = data.lineSequences.getValue(2).map { stationsById.getValue(it).stationName }

        assertEquals("#FDA6D4", data.lines.single().colorCode)
        assertEquals(listOf("天河机场", "藏龙东街", "佛祖岭"), orderedNames)
        assertEquals("Cang Long Dong Jie", stationsById.values.first { it.stationName == "藏龙东街" }.stationEnglishName)
        assertEquals("Tianhe Airport", stationsById.values.first { it.stationName == "天河机场" }.stationEnglishName)
    }
}
