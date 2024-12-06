package com.anagorny.cysnowbot.models

import java.time.LocalDateTime

data class WeatherStatus(
    val time: LocalDateTime,
    val temperature: Int,
    val temperatureUnit: String?,
    val humidity: Int,
    val humidityUnit: String?,
    val weatherCode: Int,
    val snowDepth: Double,
    val snowDepthUnit: String?,
    val snowfall: Double,
    val snowfallUnit: String?,
    val rain: Double,
    val rainUnit: String?,
    val cloudCover: Double,
    val cloudCoverUnit: String?,
    val windSpeed: Double,
    val windSpeedUnit: String?,
    val emoji: String?
)
