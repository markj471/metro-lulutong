package com.subway.app.data.local.entity

data class HistoryEntity(
    val historyId: Int = 0,
    val startStationName: String,
    val endStationName: String,
    val resultSummary: String,
    val queryTime: Long = System.currentTimeMillis()
)
