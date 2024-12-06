package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.config.WeatherCodesMappingProperties
import com.anagorny.cysnowbot.services.WeatherEmojiResolver
import org.springframework.stereotype.Service

@Service
class WeatherEmojiResolverImpl(
    private val weatherCodesProperties: WeatherCodesMappingProperties
) : WeatherEmojiResolver {
    override fun resolveEmojiByCode(weatherCode: Int?): String? =
        weatherCode?.let { weatherCodesProperties.codesMap[it]?.emoji }
}
