package com.subway.app

import com.subway.app.ui.map.MapViewportPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class MapViewportPolicyTest {

    @Test
    fun scaleIsRestrictedToSupportedRange() {
        assertEquals(1f, MapViewportPolicy.clampScale(0.25f), 0.001f)
        assertEquals(2.5f, MapViewportPolicy.clampScale(2.5f), 0.001f)
        assertEquals(4f, MapViewportPolicy.clampScale(8f), 0.001f)
    }

    @Test
    fun offsetIsCenteredWhenScaledContentFitsViewport() {
        val offset = MapViewportPolicy.clampOffset(
            viewportWidth = 400f,
            viewportHeight = 300f,
            contentWidth = 400f,
            contentHeight = 250f,
            scale = 1f,
            requestedX = 80f,
            requestedY = -60f
        )

        assertEquals(0f, offset.x, 0.001f)
        assertEquals(0f, offset.y, 0.001f)
    }

    @Test
    fun offsetIsClampedToVisibleContentBounds() {
        val offset = MapViewportPolicy.clampOffset(
            viewportWidth = 400f,
            viewportHeight = 300f,
            contentWidth = 400f,
            contentHeight = 250f,
            scale = 2f,
            requestedX = 999f,
            requestedY = -999f
        )

        assertEquals(200f, offset.x, 0.001f)
        assertEquals(-100f, offset.y, 0.001f)
    }
}
