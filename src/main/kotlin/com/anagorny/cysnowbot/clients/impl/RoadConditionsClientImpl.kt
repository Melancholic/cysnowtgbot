package com.anagorny.cysnowbot.clients.impl

import com.anagorny.cysnowbot.clients.RoadConditionsClient
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.models.RoadStateContainer
import mu.KLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class RoadConditionsClientImpl(
    @Value("road-conditions-external-service.url") val roadConditionsExternalServiceUrl: String
) : RoadConditionsClient {
    private val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")

    override fun fetchRoadConditions(): Optional<RoadConditionsContainer> {
        return try {
            val doc: Document = Jsoup.connect(roadConditionsExternalServiceUrl).get()
            Optional.of(
                RoadConditionsContainer(
                    roads = extractRoadsState(doc),
                    updatedAt = extractUpdatedTime(doc),
                )
            )
        } catch (e: Exception) {
            logger.error("Can't fetch road conditions from external service", e)
            Optional.empty()
        }
    }

    private fun extractUpdatedTime(doc: Document): LocalDateTime? = doc
        .select("#block-views-block-piste-road-conditions-block-4-2 > div > div > div > div.views-field-changed > span.field-content")
        ?.first()
        ?.`val`()
        ?.let { LocalDateTime.parse(it, FORMATTER) }

    private fun extractRoadsState(doc: Document): List<RoadStateContainer> =
        doc.select("#block-views-block-piste-road-conditions-block-4-2 > div > div > div > div.pst-rd-cont > div > div > div.field-type-list-string > div.field-label")
            ?.asSequence()
            ?.map {
                val routeParts = it.select("div.field-label")
                    ?.first()
                    ?.`val`()
                    ?.split("-") ?: emptyList()
                var src: String? = null
                var dst: String? = null
                if (routeParts.size == 2) {
                    src = routeParts[0]
                    dst = routeParts[1]
                }
                val roadStatus = it.select("div.field-items > div.field-item")?.first()?.`val`()
                return@map RoadStateContainer(
                    src = src,
                    dst = dst,
                    roadStatus = roadStatus
                )
            }
            ?.toList() ?: emptyList()


    companion object : KLogging()
}
