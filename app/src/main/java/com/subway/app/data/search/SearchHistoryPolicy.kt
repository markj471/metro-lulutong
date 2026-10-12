package com.subway.app.data.search

object SearchHistoryPolicy {
    fun add(existing: List<String>, keyword: String, maxSize: Int = 8): List<String> {
        val normalized = keyword.trim()
        if (normalized.isEmpty()) return existing.take(maxSize)
        return (listOf(normalized) + existing.filterNot { it == normalized }).take(maxSize)
    }
}
