package com.anagorny.cysnowbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.MultiValueMap

@ConfigurationProperties(prefix = "weather-provider")
data class WeatherProviderProperties(
    val url: String,
    val params: MultiValueMap<String, String>
)
