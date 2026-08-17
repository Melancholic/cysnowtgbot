package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.config.CachingConfig.Companion.WEATHER_STATUS_CACHE_NAME
import com.anagorny.cysnowbot.config.WeatherProviderProperties
import com.anagorny.cysnowbot.helpers.io
import com.anagorny.cysnowbot.models.WeatherStatus
import com.anagorny.cysnowbot.services.Fetcher
import com.anagorny.cysnowbot.services.WeatherEmojiResolver
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.databind.JsonNode
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Service
class WeatherStatusFetcherImpl(
    weatherProperties: WeatherProviderProperties,
    restTemplateBuilder: RestTemplateBuilder,
    val weatherEmojiResolver: WeatherEmojiResolver
) : Fetcher<WeatherStatus> {
    private val url = UriComponentsBuilder.fromUriString(weatherProperties.url)
        .queryParams(weatherProperties.params)
        .toUriString()

    private val restTemplate = restTemplateBuilder.build()

    @Cacheable(WEATHER_STATUS_CACHE_NAME, sync = true)
    override fun fetchAsFlow() = flow {
        emit(io { restTemplate.getForEntity(url, JsonNode::class.java) })
    }.map { response ->
        if (response.statusCode.is2xxSuccessful) {
            val body = response.body
            val units: JsonNode? = body?.get("current_units")
            val values: JsonNode? = body?.get("current")

            if (units == null) {
                logger.warn { "Couldn't weather status response: `current_units` property not found in response" }
            }

            buildResultFromJson(values, units)
        } else if (response.statusCode.is4xxClientError) {
            throw HttpClientErrorException(response.statusCode)
        } else {
            throw HttpServerErrorException(response.statusCode)
        }
    }.onEach { res -> logger.info { "Current weather status successfully fetched: $res" } }


    fun buildResultFromJson(values: JsonNode?, units: JsonNode?): WeatherStatus {
        requireNotNull(values) { "Couldn't parse weather status response: `current` property not found in response" }

        val weatherCode = values["weather_code"]?.asInt()!!

        return WeatherStatus(
            time = values["time"]?.stringValue()?.let { LocalDateTime.parse(it) }!!,
            temperature = values["temperature_2m"]?.asDouble()?.roundToInt()!!,
            temperatureUnit = units?.get("temperature_2m")?.asString(),
            humidity = values["relative_humidity_2m"]?.asDouble()?.roundToInt()!!,
            humidityUnit = units?.get("relative_humidity_2m")?.asString(),
            weatherCode = values["weather_code"]?.asInt()!!,
            snowDepth = values["snow_depth"]?.asDouble() ?: 0.0,
            snowDepthUnit = units?.get("snow_depth")?.asString(),
            snowfall = values["snowfall"]?.asDouble() ?: 0.0,
            snowfallUnit = units?.get("snowfall")?.asString(),
            rain = values["rain"]?.asDouble() ?: 0.0,
            rainUnit = units?.get("rain")?.asString(),
            cloudCover = values["cloud_cover"]?.asDouble() ?: 0.0,
            cloudCoverUnit = units?.get("cloud_cover")?.asString(),
            windSpeed = values["wind_speed_10m"]?.asDouble() ?: 0.0,
            windSpeedUnit = units?.get("wind_speed_10m")?.asString(),
            emoji = weatherEmojiResolver.resolveEmojiByCode(weatherCode)
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
