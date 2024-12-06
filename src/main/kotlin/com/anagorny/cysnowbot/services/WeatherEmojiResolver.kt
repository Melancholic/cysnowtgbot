package com.anagorny.cysnowbot.services

interface WeatherEmojiResolver {
    fun resolveEmojiByCode(weatherCode: Int?): String?
}
