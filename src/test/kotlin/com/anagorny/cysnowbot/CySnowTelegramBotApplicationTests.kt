package com.anagorny.cysnowbot

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

// Guards against Spring wiring regressions (missing beans, bad config properties,
// Jackson/RestTemplateBuilder/telegrambots breakage) across the Boot/telegrambots version
// bumps. `localhost` profile disables rate limiting; token/chat properties are dummy values,
// nothing in this test talks to the network (see NoOpTelegramBotInitializerConfig).
@SpringBootTest(
    properties = [
        "TG_BOT_TOKEN=test:token",
        "TG_BOT_NAME=test_bot",
        "TG_CHANNEL_ID=1",
        "WORK_DIR=.",
        "spring.main.allow-bean-definition-overriding=true",
    ]
)
@ActiveProfiles("localhost")
@Import(NoOpTelegramBotInitializerConfig::class)
class CySnowTelegramBotApplicationTests {

    @Test
    fun `context loads`() {
    }
}
