package com.anagorny.cysnowbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "system")
data class SystemProperties(
    val workDir: String,
    val rateLimiting: RateLimiterProperties = RateLimiterProperties(),
    val executor: ExecutorProperties = ExecutorProperties()
) {
    @ConstructorBinding
    data class ExecutorProperties(val coreSize: Int = 5, val maxSize: Int = 10)
}

@ConstructorBinding
@ConfigurationProperties(prefix = "retryer")
data class RetryerProperties(
    val maxAttempts: Int,
    val period: Duration,
    val maxPeriod: Duration,
)

data class RateLimiterProperties(
    val enabled: Boolean = true,
    val limits: Set<LimitProperties> = emptySet()
) {
    data class LimitProperties(val requests: Long, val period: Duration)
}
