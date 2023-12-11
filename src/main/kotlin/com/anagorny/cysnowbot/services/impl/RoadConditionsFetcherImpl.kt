package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.runAsync
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.models.RoadStateContainer
import com.anagorny.cysnowbot.models.RoadStatus
import com.anagorny.cysnowbot.services.RoadConditionsFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import mu.KLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class RoadConditionsFetcherImpl(
    @Qualifier("mainFlowCoroutineScope")
    private val scope: CoroutineScope,
    @Value("\${road-conditions-external-service.url}") val roadConditionsExternalServiceUrl: String
) : RoadConditionsFetcher {
    private val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")

    override suspend fun fetchRoadConditions(): Deferred<RoadConditionsContainer?> {
        return scope.runAsync {
            try {
                val doc: Document = Jsoup.connect(roadConditionsExternalServiceUrl).get()
                val result = RoadConditionsContainer(
                    roads = extractRoadsState(doc),
                    updatedAt = extractUpdatedTime(doc),
                )
                logger.info { "Current Road Conditions successfully fetched" }
                result
            } catch (e: Exception) {
                logger.error("Can't fetch road conditions from external service", e)
                null
            }
        }
    }

    private fun extractUpdatedTime(doc: Document): LocalDateTime? = doc
        .selectXpath("//*[@id=\"block-ski-cyprus-content\"]/div/div/div[2]/div[1]/span[2]/time").text()
        ?.let { LocalDateTime.parse(it, FORMATTER) }

    private fun extractRoadsState(doc: Document): List<RoadStateContainer> =
        doc.selectXpath("//*[@id=\"block-ski-cyprus-content\"]/div/div/div[2]/div[2]/div/div")
            ?.first()
            ?.children()
            ?.asSequence()
            ?.mapNotNull { line ->
                val route = line.getElementsByClass("field-label")?.text()
                    ?.split("-")

                val from = route?.getOrNull(0)?.trim()
                val to = route?.getOrNull(1)?.trim()

                val roadStatus = line.getElementsByClass("field__item")?.text()?.trim()
                return@mapNotNull RoadStateContainer(from, to, RoadStatus.parseFromText(roadStatus))
            }
            ?.toList()
            ?: emptyList()

    companion object : KLogging()
}
