package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.runAsync
import com.anagorny.cysnowbot.services.RoadConditionsFetcher
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.models.RoadStateContainer
import com.anagorny.cysnowbot.models.RoadStatus
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import mu.KLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Async
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
                RoadConditionsContainer(
                    roads = extractRoadsState(doc),
                    updatedAt = extractUpdatedTime(doc),
                )
            } catch (e: Exception) {
                logger.error("Can't fetch road conditions from external service", e)
                null
            }
        }
    }

    private fun extractUpdatedTime(doc: Document): LocalDateTime? = doc
        .select("#block-views-block-piste-road-conditions-block-4-2 > div > div > div > div.views-field-changed > span.field-content")
        ?.first()?.textNodes()?.first()?.text()
        ?.let { LocalDateTime.parse(it, FORMATTER) }

    private fun extractRoadsState(doc: Document): List<RoadStateContainer> =
        doc.select("#block-views-block-piste-road-conditions-block-4-2 > div > div > div > div.pst-rd-cont > div > div > div.field-type-list-string")
            ?.asSequence()
            ?.map {
                val routeParts = it.select("div.field-label")
                    ?.first()
                    ?.textNodes()
                    ?.first()
                    ?.text()
                    ?.split("-") ?: emptyList()
                var src: String? = null
                var dst: String? = null
                if (routeParts.size == 2) {
                    src = routeParts[0].trim()
                    dst = routeParts[1].trim()
                }
                val roadStatus = it.select("div.field-items > div.field-item")?.first()?.textNodes()?.first()?.text()
                return@map RoadStateContainer(
                    src = src,
                    dst = dst,
                    roadStatus = RoadStatus.parseFromText(roadStatus)
                )
            }
            ?.toList() ?: emptyList()


    companion object : KLogging()
}
