package com.anagorny.cysnowbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegram")
data class TelegramProperties(
    val chatId: String,
    val bot: BotProperties
) {
    data class BotProperties(
        val token: String,
        val name: String
    )
}
