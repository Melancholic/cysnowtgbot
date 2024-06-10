package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.config.WeatherProviderProperties
import com.anagorny.cysnowbot.models.WeatherStatus
import com.anagorny.cysnowbot.services.Fetcher
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.util.UriComponentsBuilder

@Service
class WeatherStatusFetcherImpl(
    weatherProperties: WeatherProviderProperties,
    restTemplateBuilder: RestTemplateBuilder
) : Fetcher<WeatherStatus> {
    private val url = UriComponentsBuilder.fromHttpUrl(weatherProperties.url)
        .queryParams(weatherProperties.params)
        .toUriString()

    private val restTemplate = restTemplateBuilder.build()

    override fun fetchAsFlow(): Flow<WeatherStatus?> {
        return flow {
            emit(withContext(Dispatchers.IO) {
                restTemplate.getForEntity(url, JsonNode::class.java)
            })
        }.map { response ->
            if (response.statusCode.is2xxSuccessful) {
                val body = response.body
                val units: JsonNode? = body?.get("current_units")
                val values: JsonNode? = body?.get("current")

                if (units == null) {
                    logger.warn { "Couldn't weather status response: `current_units` property not found in response" }
                }

                if (values == null) {
                    logger.warn { "Couldn't weather status response: `current` property not found in response" }
                }


                WeatherStatus.fromJson(values, units)
            } else if (response.statusCode.is4xxClientError) {
                throw HttpClientErrorException(response.statusCode)
            } else {
                throw HttpServerErrorException(response.statusCode)
            }
        }.catch { e ->
            logger.error(e) { "Error while updating weather in Olympus's top" }
            emit(null)
        }.onEach { res -> logger.info { "Current weather status successfully fetched: $res" } }
    }

    companion object : KLogging()
}
