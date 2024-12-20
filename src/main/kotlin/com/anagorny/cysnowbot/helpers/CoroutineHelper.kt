package com.anagorny.cysnowbot.helpers

import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


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
    return CoroutineScope(context)
}
