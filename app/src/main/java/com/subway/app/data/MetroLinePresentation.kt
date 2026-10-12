package com.subway.app.data

object MetroLinePresentation {
    private val publicColors = mapOf(
        "1号线" to "#267AC3",
        "2号线" to "#FDA6D4",
        "3号线" to "#E6BC78",
        "4号线" to "#9BCC45",
        "5号线" to "#DA383E",
        "6号线" to "#147D3F",
        "7号线" to "#FC9728",
        "8号线" to "#9CC2CD",
        "11号线" to "#FDCB33",
        "12号线" to "#41A3E5",
        "16号线" to "#EC67C0",
        "19号线" to "#5FC1A8",
        "阳逻线" to "#C33182"
    )

    private val publicOrigins = mapOf(
        "1号线" to "径河",
        "2号线" to "天河机场",
        "3号线" to "宏图大道",
        "4号线" to "武汉火车站",
        "5号线" to "红霞",
        "6号线" to "东风公司",
        "7号线" to "黄陂广场",
        "8号线" to "金潭路",
        "11号线" to "江安路",
        "12号线" to "钢都花园",
        "16号线" to "通航机场",
        "19号线" to "武汉站西广场",
        "阳逻线" to "后湖大道"
    )

    private val publicDestinations = mapOf(
        "1号线" to "汉口北",
        "2号线" to "佛祖岭",
        "3号线" to "沌阳大道",
        "4号线" to "柏林",
        "5号线" to "武汉站东广场",
        "6号线" to "新城十一路",
        "7号线" to "青龙山地铁小镇",
        "8号线" to "军运村",
        "11号线" to "葛店南站",
        "12号线" to "墨水湖公园",
        "16号线" to "国博中心南",
        "19号线" to "新月溪公园",
        "阳逻线" to "金台"
    )

    fun normalizeColorCode(raw: String): String {
        val value = raw.trim()
        return if (value.startsWith("#")) value else "#$value"
    }

    fun displayColorCode(lineName: String, sourceColor: String): String {
        return publicColors[lineName] ?: normalizeColorCode(sourceColor)
    }

    fun rangeText(lineName: String): String {
        val origin = publicOrigins[lineName] ?: return ""
        val destination = publicDestinations[lineName] ?: return ""
        return "$origin ↔ $destination"
    }

    fun countLineStations(lineIdsByStation: List<String>): Int {
        return lineIdsByStation.sumOf { lineIds ->
            lineIds.split(",").count { it.trim().isNotEmpty() }
        }
    }

    fun foregroundColorCode(lineName: String): String {
        val color = publicColors[lineName] ?: return "#FFFFFF"
        val rgb = color.removePrefix("#").toInt(16)
        fun channel(shift: Int): Double {
            val value = ((rgb shr shift) and 0xFF) / 255.0
            return if (value <= 0.04045) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)
        }
        val luminance = 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
        return if (luminance > 0.42) "#1A1A1A" else "#FFFFFF"
    }

    fun <T> orderStations(lineName: String, stations: List<T>, stationName: (T) -> String): List<T> {
        val origin = publicOrigins[lineName] ?: return stations
        return when {
            stations.firstOrNull()?.let(stationName) == origin -> stations
            stations.lastOrNull()?.let(stationName) == origin -> stations.asReversed()
            else -> stations
        }
    }

    fun orderStationNames(lineName: String, stationNames: List<String>): List<String> {
        return orderStations(lineName, stationNames) { it }
    }
}
