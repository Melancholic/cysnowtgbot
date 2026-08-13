package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.models.RoadStatus
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime

class RoadConditionsParserTest {

    // Real save of https://www.cyprusski.com/piste-road-conditions, in the layout that broke the old positional XPath.
    private fun loadFixture() = Jsoup.parse(
        File("src/test/resources/fixtures/road-conditions-page.html"),
        "UTF-8",
        "https://www.cyprusski.com/"
    )

    @Test
    fun `parses all roads and their status from the live page layout`() {
        val result = RoadConditionsParser.parse(loadFixture())

        assertEquals(6, result.roads.size)
        assertEquals(
            listOf(
                "Karvounas" to "Troodos",
                "Platres" to "Troodos",
                "Prodromos" to "Dias",
                "Troodos" to "Sun Valley",
                "Troodos" to "Dias",
                "Prodromos" to "Sun Valley",
            ),
            result.roads.map { it.src to it.dst }
        )
        assertEquals(6, result.roads.count { it.roadStatus == RoadStatus.OPEN })
    }

    @Test
    fun `parses updatedAt from the time element's datetime attribute`() {
        val result = RoadConditionsParser.parse(loadFixture())

        assertEquals(LocalDateTime.of(2026, 5, 10, 10, 34, 3), result.updatedAt)
    }

    @Test
    fun `throws instead of returning an empty result when road rows can't be found`() {
        val emptyDoc = Jsoup.parse("<html><body>nothing here</body></html>")

        assertThrows(IllegalStateException::class.java) { RoadConditionsParser.parse(emptyDoc) }
    }
}
