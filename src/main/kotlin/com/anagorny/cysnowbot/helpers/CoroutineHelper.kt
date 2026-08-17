package com.anagorny.cysnowbot.helpers

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {}

suspend fun <T> io(block: CoroutineScope.() -> T) = withContext(Dispatchers.IO + MDCContext()) { block() }

fun <T> CoroutineScope.runAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T,
) = async(context + MDCContext(), start, block)

fun CoroutineScope.launchAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
) = launch(context + MDCContext(), start, block)

fun coroutineScope(coreSize: Int, maxSize: Int): CoroutineScope {
    val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
    threadPoolTaskExecutor.corePoolSize = coreSize
    threadPoolTaskExecutor.maxPoolSize = maxSize
    threadPoolTaskExecutor.initialize()
    val context = threadPoolTaskExecutor.asCoroutineDispatcher()
    // Without SupervisorJob one uncaught throwable cancels this shared scope, and every later
    // launch on it silently does nothing.
    val handler = CoroutineExceptionHandler { ctx, e ->
        logger.error(e) { "Uncaught exception in coroutine '${ctx[CoroutineName]?.name ?: "unnamed"}'" }
    }
    return CoroutineScope(SupervisorJob() + context + handler)
}
