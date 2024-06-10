package com.anagorny.cysnowbot.models

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import kotlin.math.roundToInt

data class WeatherStatus(
    val time: LocalDateTime,
    val temperature: Int,
    val temperatureUnit: String?,
    val humidity: Int,
    val humidityUnit: String?,
    val weatherCode: Int,
    val snowDepth: Double,
    val snowDepthUnit: String?,
    val snowfall: Double?,
    val rain: Double?,
    val cloudCover: Double?,
    val windSpeed: Double?,
) {
    companion object {
        fun fromJson(values: JsonNode?, units: JsonNode?): WeatherStatus? {
            if (values == null) {
                return null
            }
            val time = values["time"]?.textValue()?.let { LocalDateTime.parse(it) }!!
            val temperature = values["temperature_2m"]?.asDouble()?.roundToInt()!!
            val temperatureUnit = units?.get("temperature_2m")?.asText()
            val humidity = values["relative_humidity_2m"]?.asDouble()?.roundToInt()!!
            val humidityUnit = units?.get("relative_humidity_2m")?.asText()
            val weatherCode = values["weather_code"]?.asInt()!!
            val snowDepth = values["snow_depth"]?.asDouble()!!
            val snowDepthUnit = units?.get("snow_depth")?.asText()
            val snowfall = values["snowfall"]?.asDouble()
            val rain = values["rain"]?.asDouble()
            val cloudCover = values["cloud_cover"]?.asDouble()
            val windSpeed = values["wind_speed_10m"]?.asDouble()

//            val rain
//            val showers
//            val snowfall
//            val weatherCode
//            val cloudCover
//            val windSpeed
//            val windGusts

            return WeatherStatus(
                time = time,
                temperature = temperature,
                temperatureUnit = temperatureUnit,
                humidity = humidity,
                humidityUnit = humidityUnit,
                weatherCode = weatherCode,
                snowDepth = snowDepth,
                snowDepthUnit = snowDepthUnit,
                snowfall = snowfall,
                rain = rain,
                cloudCover = cloudCover,
                windSpeed = windSpeed
            )
        }
    }
}
