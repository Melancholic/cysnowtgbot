package com.anagorny.cysnowbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "weather")
data class WeatherCodesMappingProperties (
    val codes: List<WeatherCodeMapping>
) {
    val codesMap: Map<Int, WeatherCodeMapping> = codes.associateBy { it.code }
}

data class WeatherCodeMapping (
    val code: Int,
    val text: String,
    val emoji: String
)
