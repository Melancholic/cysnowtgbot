package com.anagorny.cysnowbot

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.longpolling.starter.TelegramBotInitializer

// TelegramBotStarterConfiguration's real TelegramBotInitializer calls registerBot(), which
// still clears the webhook synchronously (BotSession.start() -> executeDeleteWebhook) from
// an InitializingBean, so it fails context startup in a network-isolated test run - same
// issue as the 6.8.0 starter, just moved to a different class. Override the bean by name
// with a no-op variant so the context can actually be exercised.
@TestConfiguration
class NoOpTelegramBotInitializerConfig {

    @Bean
    fun telegramBotInitializer(
        telegramBotsApplication: TelegramBotsLongPollingApplication,
        longPollingBots: ObjectProvider<List<SpringLongPollingBot>>
    ): TelegramBotInitializer =
        object : TelegramBotInitializer(
            telegramBotsApplication,
            longPollingBots.getIfAvailable { emptyList() }
        ) {
            override fun afterPropertiesSet() {
                // no-op: skip real bot registration in tests
            }
        }
}
