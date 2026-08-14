package com.anagorny.cysnowbot

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.generics.LongPollingBot
import org.telegram.telegrambots.starter.SpringWebhookBot
import org.telegram.telegrambots.starter.TelegramBotInitializer

// TelegramBotStarterConfiguration's real TelegramBotInitializer calls the Telegram API
// (deleteWebhook) synchronously from an InitializingBean, which fails context startup
// in a network-isolated test run. Override the bean by name with a no-op variant so
// the context can actually be exercised; this whole file goes away once telegrambots
// 10.2.0's SpringLongPollingBot starter replaces the 6.8.0 starter workaround.
@TestConfiguration
class NoOpTelegramBotInitializerConfig {

    @Bean
    fun telegramBotInitializer(
        telegramBotsApi: TelegramBotsApi,
        longPollingBots: ObjectProvider<List<LongPollingBot>>,
        webHookBots: ObjectProvider<List<SpringWebhookBot>>
    ): TelegramBotInitializer =
        object : TelegramBotInitializer(
            telegramBotsApi,
            longPollingBots.getIfAvailable { emptyList() },
            webHookBots.getIfAvailable { emptyList() }
        ) {
            override fun afterPropertiesSet() {
                // no-op: skip real bot registration in tests
            }
        }
}
