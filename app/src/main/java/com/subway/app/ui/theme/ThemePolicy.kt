package com.subway.app.ui.theme

import com.subway.app.data.settings.ThemeMode

object ThemePolicy {
    fun useDarkTheme(themeMode: ThemeMode, systemDark: Boolean): Boolean {
        return when (themeMode) {
            ThemeMode.SYSTEM -> systemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }
}
