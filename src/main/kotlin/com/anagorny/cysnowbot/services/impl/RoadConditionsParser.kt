package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.models.RoadStateContainer
import com.anagorny.cysnowbot.models.RoadStatus
import mu.KotlinLogging
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

// Selects by paragraph type instead of a positional XPath: the site renders the road-conditions
// paragraph twice (main content + footer block) and leaves one copy empty, so we also pick
// whichever match is non-empty.
object RoadConditionsParser {
    private val logger = KotlinLogging.logger {}
    private val legacyTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
    private const val ROAD_PARAGRAPH_SELECTOR = "div.paragraph--type--weather-and-piste-conditions"

    fun parse(doc: Document): RoadConditionsContainer {
        val roads = extractRoads(doc)
        check(roads.isNotEmpty()) { "No road rows found on the page - site markup may have changed" }
        return RoadConditionsContainer(roads = roads, updatedAt = extractUpdatedAt(doc))
    }

    private fun extractRoads(doc: Document): List<RoadStateContainer> =
        doc.select(ROAD_PARAGRAPH_SELECTOR)
            .map { it.select("div.field") }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
            .mapNotNull(::toRoadState)

    private fun toRoadState(field: Element): RoadStateContainer? {
        val label = field.selectFirst(".field-label")?.text()?.trim()?.takeIf { it.isNotEmpty() }
            ?: return null
        val (from, to) = label.split(Regex("\\s*[-\u2013\u2014]\\s*"), limit = 2)
            .let { it.getOrNull(0)?.trim() to it.getOrNull(1)?.trim() }
        val statusText = field.selectFirst(".field__item")?.text()?.trim()
        return RoadStateContainer(from, to, RoadStatus.parseFromText(statusText))
    }

    private fun extractUpdatedAt(doc: Document): LocalDateTime? {
        val time = doc.select(ROAD_PARAGRAPH_SELECTOR).firstOrNull()
            ?.parents()
            ?.firstOrNull { it.hasClass("Piste-Road-Cond") || it.hasClass("Piste-Road-Cond-blk") }
            ?.selectFirst("time")
            ?: return null

        time.attr("datetime").takeIf { it.isNotBlank() }?.let { iso ->
            runCatching { return OffsetDateTime.parse(iso).toLocalDateTime() }
                .onFailure { logger.warn(it) { "Could not parse time 'datetime' attribute '$iso'" } }
        }
        return time.text().trim().takeIf { it.isNotEmpty() }
            ?.let { runCatching { LocalDateTime.parse(it, legacyTimeFormat) }.getOrNull() }
    }
}
