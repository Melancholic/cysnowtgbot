package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.config.CachingConfig.Companion.ROAD_CONDITION_CACHE_NAME
import com.anagorny.cysnowbot.helpers.io
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.services.Fetcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class RoadConditionsFetcherImpl(
    @Value("\${road-conditions-external-service.url}") val roadConditionsExternalServiceUrl: String
) : Fetcher<RoadConditionsContainer> {

    @Cacheable(ROAD_CONDITION_CACHE_NAME, sync = true)
    override fun fetchAsFlow() = flow {
        val result = io {
            val doc = Jsoup.connect(roadConditionsExternalServiceUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .get()
            RoadConditionsParser.parse(doc)
        }
        logger.info { "Current Road Conditions successfully fetched" }
        emit(result)
    }

    companion object {
        val logger = KotlinLogging.logger {}
        private const val TIMEOUT_MILLIS = 15_000
        private const val USER_AGENT =
            "Mozilla/5.0 (compatible;)"
    }
}
