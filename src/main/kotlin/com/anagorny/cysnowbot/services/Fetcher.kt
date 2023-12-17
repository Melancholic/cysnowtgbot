package com.anagorny.cysnowbot.services

import kotlinx.coroutines.Deferred

interface Fetcher<T> {
    suspend fun fetchAsync(): Deferred<T?>
}
