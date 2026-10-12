package com.subway.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object LineList : Screen("line_list")
    object LineDetail : Screen("line_detail/{lineId}") {
        fun createRoute(lineId: Int) = "line_detail/$lineId"
    }
    object StationSearch : Screen("station_search")
    object StationDetail : Screen("station_detail/{stationId}") {
        fun createRoute(stationId: Int) = "station_detail/$stationId"
    }
    object RoutePlan : Screen("route_plan")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object ChangePassword : Screen("change_password")
}
