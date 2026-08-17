package com.anagorny.cysnowbot.services

import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.NotNull

/**
 * Implementations must never emit `null` and must let failures propagate: these methods are
 * `@Cacheable(sync = true)`, Spring adapts the [Flow] to a `Flux` (which forbids nulls), and
 * sync mode has no `unless`, so throwing is the only way to keep a failure out of the cache.
 */
interface Fetcher<T : Any> {
    fun fetchAsFlow(): Flow<@NotNull T>
}
