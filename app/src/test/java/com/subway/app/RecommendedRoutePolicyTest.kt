package com.subway.app

import com.subway.app.viewmodel.RecommendedRoutePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendedRoutePolicyTest {

    @Test
    fun recommendedRouteUsesStableTransferPenalty() {
        assertEquals(2, RecommendedRoutePolicy.TRANSFER_PENALTY)
    }
}
