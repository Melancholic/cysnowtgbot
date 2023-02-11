package com.anagorny.cysnowbot.config

import com.anagorny.cysnowbot.helpers.coroutineScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncListenableTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.TimeUnit


@Configuration
class SpringConfiguration(
    val properties: SystemProperties
) {
    @Bean
    fun threadPoolTaskExecutor(): AsyncListenableTaskExecutor {
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
    fun jsonMapper(): ObjectMapper = ObjectMapper()
        .registerModule(
            KotlinModule.Builder()
                .build()
        )
}
