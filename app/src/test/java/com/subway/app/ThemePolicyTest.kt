package com.subway.app

import com.subway.app.data.settings.ThemeMode
import com.subway.app.ui.theme.ThemePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePolicyTest {

    @Test
    fun explicitThemeModesOverrideSystemTheme() {
        assertFalse(ThemePolicy.useDarkTheme(ThemeMode.LIGHT, systemDark = true))
        assertTrue(ThemePolicy.useDarkTheme(ThemeMode.DARK, systemDark = false))
    }

    @Test
    fun systemThemeModeFollowsSystem() {
        assertFalse(ThemePolicy.useDarkTheme(ThemeMode.SYSTEM, systemDark = false))
        assertTrue(ThemePolicy.useDarkTheme(ThemeMode.SYSTEM, systemDark = true))
    }
}
