package com.subway.app.data.settings

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色")
}

class AppSettingsManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    fun themeMode(): ThemeMode {
        return runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun fontScale(): Float = prefs.getFloat(KEY_FONT_SCALE, 1.0f)

    fun setFontScale(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SCALE, scale.coerceIn(0.9f, 1.2f)).apply()
    }

    fun cacheSizeText(): String {
        val bytes = folderSize(appContext.cacheDir)
        return if (bytes < 1024 * 1024) {
            "%.1f KB".format(bytes / 1024.0)
        } else {
            "%.1f MB".format(bytes / 1024.0 / 1024.0)
        }
    }

    fun clearCache() {
        appContext.cacheDir?.deleteRecursively()
    }

    private fun folderSize(file: java.io.File?): Long {
        if (file == null || !file.exists()) return 0L
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf { folderSize(it) } ?: 0L
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_FONT_SCALE = "font_scale"
    }
}
