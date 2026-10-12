package com.subway.app.data.local.entity

import com.subway.app.data.StationSchedule

/**
 * 站点实体
 * @property stationId 站点唯一编号
 * @property stationName 站点名称
 * @property lineIds 所属线路（逗号分隔）
 * @property latitude 纬度
 * @property longitude 经度
 * @property isTransfer 是否为换乘站
 * @property firstTrain 首班车时间（如 "06:02"）
 * @property lastTrain 末班车时间（如 "22:52"）
 */
data class StationEntity(
    val stationId: Int,
    val stationName: String,
    val lineIds: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isTransfer: Boolean = false,
    val firstTrain: String = "",
    val lastTrain: String = "",
    val schedules: List<StationSchedule> = emptyList(),
    val stationEnglishName: String = ""
)
