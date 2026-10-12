package com.subway.app.ui.map

import kotlin.math.max

data class MapOffset(val x: Float, val y: Float)

object MapViewportPolicy {
    const val MIN_SCALE = 1f
    const val MAX_SCALE = 4f

    fun clampScale(scale: Float): Float = scale.coerceIn(MIN_SCALE, MAX_SCALE)

    fun clampOffset(
        viewportWidth: Float,
        viewportHeight: Float,
        contentWidth: Float,
        contentHeight: Float,
        scale: Float,
        requestedX: Float,
        requestedY: Float
    ): MapOffset {
        val safeScale = clampScale(scale)
        val maxX = max(0f, (contentWidth * safeScale - viewportWidth) / 2f)
        val maxY = max(0f, (contentHeight * safeScale - viewportHeight) / 2f)
        return MapOffset(
            x = requestedX.coerceIn(-maxX, maxX),
            y = requestedY.coerceIn(-maxY, maxY)
        )
    }
}
