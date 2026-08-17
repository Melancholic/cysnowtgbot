package com.anagorny.cysnowbot

import com.anagorny.cysnowbot.config.CachingConfig
import com.anagorny.cysnowbot.config.CachingConfig.Companion.CAMERA_SNAPSHOT_CACHE_NAME
import com.anagorny.cysnowbot.config.CachingConfig.Companion.ROAD_CONDITION_CACHE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Pins down that `@Cacheable` materializes a `Flow` instead of caching the cold `Flow` object.
 * It only does so because kotlinx-coroutines-reactor on the classpath makes Spring treat `Flow`
 * as a reactive type - drop that dependency and the caching silently stops working.
 */
@SpringJUnitConfig(classes = [CachingConfig::class, CacheableFlowTest.TestConfig::class])
class CacheableFlowTest {

    @Configuration
    @EnableCaching
    open class TestConfig {
        @Bean
        open fun countingFetcher() = CountingFetcher()

        @Bean
        open fun failingFetcher() = FailingFetcher()
    }

    // State must be static: these beans are CGLIB-proxied, and the proxy is built without running
    // field initializers, so instance fields read as null through the reference the test holds.
    open class CountingFetcher {
        @Cacheable(ROAD_CONDITION_CACHE_NAME, sync = true)
        open fun fetchAsFlow(): Flow<String> = flow {
            calls.incrementAndGet()
            emit("value")
        }

        companion object {
            val calls = AtomicInteger()
        }
    }

    open class FailingFetcher {
        @Cacheable(CAMERA_SNAPSHOT_CACHE_NAME, sync = true)
        open fun fetchAsFlow(): Flow<String> = flow {
            calls.incrementAndGet()
            check(!shouldFail.get()) { "upstream is down" }
            emit("value")
        }

        companion object {
            val calls = AtomicInteger()
            val shouldFail = AtomicBoolean(true)
        }
    }

    @Autowired
    lateinit var countingFetcher: CountingFetcher

    @Autowired
    lateinit var failingFetcher: FailingFetcher

    @Test
    fun `flow is materialized once, not re-executed per collect`() = runBlocking {
        assertEquals("value", countingFetcher.fetchAsFlow().first())
        assertEquals("value", countingFetcher.fetchAsFlow().first())

        // 2 would mean Spring cached the cold Flow object and the fetch ran on every collect.
        assertEquals(1, CountingFetcher.calls.get())
    }

    @Test
    fun `failures are not cached`() = runBlocking {
        // Throws from fetchAsFlow() itself, not the collect - Caffeine runs the loader
        // synchronously inside its compute, so it never gets as far as returning a Flow.
        assertFailsWith<IllegalStateException> { failingFetcher.fetchAsFlow().first() }

        FailingFetcher.shouldFail.set(false)
        assertEquals("value", failingFetcher.fetchAsFlow().first())
        assertEquals(2, FailingFetcher.calls.get())
    }
}
