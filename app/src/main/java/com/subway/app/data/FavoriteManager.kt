package com.subway.app.data
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Favorite(val label: String, val stationName: String)

class FavoriteManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAll(): List<Favorite> {
        val json = prefs.getString("list", "[]") ?: "[]"
        val type = object : TypeToken<List<Favorite>>() {}.type
        return try { gson.fromJson(json, type) } catch (_: Exception) { emptyList() }
    }

    fun add(fav: Favorite) {
        val list = getAll().toMutableList()
        list.removeAll { it.stationName == fav.stationName }
        list.add(0, fav)
        save(list)
    }

    fun remove(stationName: String) {
        save(getAll().filter { it.stationName != stationName })
    }

    fun getTop(n: Int = 3): List<Favorite> = getAll().take(n)

    private fun save(list: List<Favorite>) {
        prefs.edit().putString("list", gson.toJson(list)).apply()
    }
}
