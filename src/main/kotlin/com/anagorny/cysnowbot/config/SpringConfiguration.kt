package com.anagorny.cysnowbot.config

import com.anagorny.cysnowbot.helpers.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.boot.restclient.RestTemplateBuilder
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration


@Configuration
//ToDo remove it after migration telegrambots-spring-boot-starter to spring boot 3.0
@Import(value = [TelegramBotStarterConfiguration::class])
class SpringConfiguration(
    val properties: SystemProperties
) {
    @Bean
    fun threadPoolTaskExecutor(): AsyncTaskExecutor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
        threadPoolTaskExecutor.corePoolSize = properties.executor.coreSize
        threadPoolTaskExecutor.maxPoolSize = properties.executor.maxSize
        return threadPoolTaskExecutor
    }

    @Bean
    fun mainFlowCoroutineScope(): CoroutineScope = coroutineScope(
        properties.executor.coreSize,
        properties.executor.maxSize
    ) + MDCContext()

    @Bean
    fun jsonMapper(): ObjectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()

    @Bean
    fun  restTemplateBuilder(systemProperties: SystemProperties) : RestTemplateBuilder {
        return RestTemplateBuilder()
            .connectTimeout(properties.timeouts.connect)
            .readTimeout(properties.timeouts.read)
    }
}
