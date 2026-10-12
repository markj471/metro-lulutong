package com.subway.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class StationSchedule(
    val lineName: String,
    val direction: String,
    val serviceDay: String,
    val firstTrain: String,
    val lastTrain: String
)

private data class StationScheduleRecord(
    val stationName: String,
    val schedules: List<StationSchedule>
)

object WuhanStationScheduleLoader {
    fun loadFromAssets(context: Context): Map<String, List<StationSchedule>> {
        val json = context.assets.open("wuhan_station_schedules.json").bufferedReader().readText()
        return parse(json)
    }

    fun parse(json: String): Map<String, List<StationSchedule>> {
        val type = object : TypeToken<List<StationScheduleRecord>>() {}.type
        val records: List<StationScheduleRecord> = Gson().fromJson(json, type) ?: emptyList()
        return records.associate { it.stationName to it.schedules }
    }
}
