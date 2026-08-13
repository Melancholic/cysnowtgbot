package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.services.Fetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import mu.KLogging
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RoadConditionsFetcherImpl(
    @Qualifier("mainFlowCoroutineScope")
    private val scope: CoroutineScope,
    @Value("\${road-conditions-external-service.url}") val roadConditionsExternalServiceUrl: String
) : Fetcher<RoadConditionsContainer> {

    override fun fetchAsFlow(): Flow<RoadConditionsContainer?> {
        return flow {
            try {
                val doc = Jsoup.connect(roadConditionsExternalServiceUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MILLIS)
                    .get()
                val result = RoadConditionsParser.parse(doc)
                logger.info { "Current Road Conditions successfully fetched" }
                emit(result)
            } catch (e: Exception) {
                logger.error(e) { "Can't fetch road conditions from external service" }
                emit(null)
            }
        }.catch { e ->
            logger.error(e) { "Error while updating state of road conditions" }
            emit(null)
        }
    }

    companion object : KLogging() {
        private const val TIMEOUT_MILLIS = 15_000
        private const val USER_AGENT =
            "Mozilla/5.0 (compatible;)"
    }
}
