package com.anagorny.cysnowbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "system")
data class SystemProperties(
    val workDir: String,
    val rateLimiting: RateLimiterProperties = RateLimiterProperties(),
    val executor: ExecutorProperties = ExecutorProperties(),
    val retryer: RetryerProperties = RetryerProperties()
) {
    data class ExecutorProperties(val coreSize: Int = 5, val maxSize: Int = 10)
}

data class RetryerProperties(
    val maxAttempts: Int = 3,
    val period: Duration = Duration.ofSeconds(1),
    val maxPeriod: Duration = Duration.ofSeconds(5),
)

data class RateLimiterProperties(
    val enabled: Boolean = true,
    val limits: Set<LimitProperties> = emptySet()
) {
    data class LimitProperties(val requests: Long, val period: Duration)
}
