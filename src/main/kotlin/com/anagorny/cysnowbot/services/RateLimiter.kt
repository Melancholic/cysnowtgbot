package com.anagorny.cysnowbot.services

import java.time.Duration


interface RateLimiter {
    fun isRequestAllowed(apiKey: String): Boolean
    fun howLongForAllow(apiKey: String): Duration
    fun isRateLimitingEnabled(): Boolean
}
