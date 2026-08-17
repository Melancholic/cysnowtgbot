package com.anagorny.cysnowbot.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CachingConfig {

    @Bean
    fun cameraSnapshotFetcherCache() = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .initialCapacity(1)
        .maximumSize(1)

    @Bean
    fun roadConditionsFetcherCache() = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .initialCapacity(1)
        .maximumSize(1)

    @Bean
    fun weatherStatusFetcherCache() = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .initialCapacity(1)
        .maximumSize(1)


    @Bean
    fun cacheManager() = CaffeineCacheManager().apply {
        registerCustomCache(CAMERA_SNAPSHOT_CACHE_NAME, cameraSnapshotFetcherCache().buildAsync())
        registerCustomCache(ROAD_CONDITION_CACHE_NAME, roadConditionsFetcherCache().buildAsync())
        registerCustomCache(WEATHER_STATUS_CACHE_NAME, weatherStatusFetcherCache().buildAsync())
    }


    companion object {
        const val CAMERA_SNAPSHOT_CACHE_NAME = "camera-snapshot-fetcher-cache"
        const val ROAD_CONDITION_CACHE_NAME = "road-condition-fetcher-cache"
        const val WEATHER_STATUS_CACHE_NAME = "weather-status-fetcher-cache"
    }
}
