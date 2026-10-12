package com.subway.app.data.local.entity

data class ConnectionEntity(
    val id: Int = 0,
    val fromStationId: Int,
    val toStationId: Int,
    val weight: Int,
    val lineId: Int
)
