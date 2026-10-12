package com.subway.app

import com.subway.app.data.MetroLinePresentation
import org.junit.Assert.assertEquals
import org.junit.Test

class MetroLinePresentationTest {

    @Test
    fun colorCodeWithoutHashIsNormalized() {
        assertEquals("#EB81B9", MetroLinePresentation.normalizeColorCode("EB81B9"))
        assertEquals("#3080B7", MetroLinePresentation.normalizeColorCode("#3080B7"))
    }

    @Test
    fun websiteLineColorOverridesMapSourceColor() {
        assertEquals("#267AC3", MetroLinePresentation.displayColorCode("1号线", "3080B7"))
        assertEquals("#FDA6D4", MetroLinePresentation.displayColorCode("2号线", "EB81B9"))
        assertEquals("#C33182", MetroLinePresentation.displayColorCode("阳逻线", "D10195"))
    }

    @Test
    fun lineTwoIsDisplayedFromTianheAirportToFozuling() {
        val sourceOrder = listOf("佛祖岭", "藏龙东街", "天河机场")

        val displayOrder = MetroLinePresentation.orderStationNames("2号线", sourceOrder)

        assertEquals(listOf("天河机场", "藏龙东街", "佛祖岭"), displayOrder)
    }

    @Test
    fun aLineAlreadyStartingAtItsPublicOriginKeepsItsOrder() {
        val sourceOrder = listOf("东风公司", "码头潭公园", "新城十一路")

        val displayOrder = MetroLinePresentation.orderStationNames("6号线", sourceOrder)

        assertEquals(sourceOrder, displayOrder)
    }

    @Test
    fun publicRangeTextUsesCurrentFullLineEndpoints() {
        assertEquals("天河机场 ↔ 佛祖岭", MetroLinePresentation.rangeText("2号线"))
        assertEquals("武汉站西广场 ↔ 新月溪公园", MetroLinePresentation.rangeText("19号线"))
    }

    @Test
    fun publishedStationCountIncludesEachLineMembership() {
        assertEquals(4, MetroLinePresentation.countLineStations(listOf("1", "1,2", "2")))
    }

    @Test
    fun lightLineColorsUseDarkForegroundText() {
        assertEquals("#1A1A1A", MetroLinePresentation.foregroundColorCode("2号线"))
        assertEquals("#1A1A1A", MetroLinePresentation.foregroundColorCode("11号线"))
        assertEquals("#FFFFFF", MetroLinePresentation.foregroundColorCode("1号线"))
    }
}
