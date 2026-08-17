package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.io
import com.anagorny.cysnowbot.helpers.removeFile
import com.anagorny.cysnowbot.helpers.runAsync
import com.anagorny.cysnowbot.models.AggregatedDataContainer
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.models.WeatherStatus
import com.anagorny.cysnowbot.services.DataHolder
import com.anagorny.cysnowbot.services.Fetcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

@Service
class DataHolderImpl(
    private val cameraSnapshotFetcher: Fetcher<CameraSnapshotContainer>,
    private val roadConditionsFetcher: Fetcher<RoadConditionsContainer>,
    private val olympusWeatherStatusFetcher: Fetcher<WeatherStatus>,
    @Qualifier("mainFlowCoroutineScope") private val scope: CoroutineScope
) : DataHolder {
    private val aggregatedData = AtomicReference<AggregatedDataContainer?>()
    private val inFlight = AtomicReference<Job?>()
    private val commitMutex = Mutex()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        updateState()
    }

    override fun getData(): AggregatedDataContainer {
        val current = aggregatedData.get()
        if (current != null) {
            updateState()
            return current
        }
        return runBlocking {
            withTimeoutOrNull(COLD_START_TIMEOUT) { updateState().join() }
            aggregatedData.get() ?: AggregatedDataContainer()
        }
    }

    fun updateState(): Job {
        while (true) {
            val existing = inFlight.get()
            if (existing?.isActive == true) return existing
            // LAZY: a job that loses the CAS must never have started, or it races the winner.
            val job = scope.launch(start = CoroutineStart.LAZY) { refresh() }
            if (inFlight.compareAndSet(existing, job)) {
                job.start()
                return job
            }
            job.cancel()
        }
    }

    private suspend fun refresh() {
        logger.debug { "Refresh state starting" }
        try {
            commit(withTimeout(REFRESH_TIMEOUT) { fetchAll(aggregatedData.get()) })
            logger.info { "Refreshing state finished" }
        } catch (e: TimeoutCancellationException) {
            logger.error(e) { "Refreshing state timed out after $REFRESH_TIMEOUT" }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Refreshing state failed" }
        }
    }

    private suspend fun fetchAll(previous: AggregatedDataContainer?): AggregatedDataContainer =
        coroutineScope {
            val roads = runAsync { roadConditionsFetcher.fetchOrNull() }
            val camera = runAsync { cameraSnapshotFetcher.fetchOrNull() }
            val weather = runAsync { olympusWeatherStatusFetcher.fetchOrNull() }

            AggregatedDataContainer.builder(previous)
                .roadConditions(roads.await())
                .cameraSnapshot(camera.await())
                .olympusWeatherStatus(weather.await())
                .build()
        }

    private suspend fun <T : Any> Fetcher<T>.fetchOrNull(): T? = try {
        fetchAsFlow().firstOrNull()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.error(e) { "Fetch failed: ${this::class.simpleName}" }
        null
    }

    private suspend fun commit(result: AggregatedDataContainer) = commitMutex.withLock {
        val old = aggregatedData.get()
        // An earlier-started refresh must not clobber a newer one that already landed.
        if (old != null && !result.timestamp.isAfter(old.timestamp)) {
            io { releaseImage(discarded = result, keeper = old) }
            return@withLock
        }
        aggregatedData.set(result)
        io { releaseImage(discarded = old, keeper = result) }
    }

    private fun releaseImage(discarded: AggregatedDataContainer?, keeper: AggregatedDataContainer) {
        val image = discarded?.cameraSnapshot?.image
        // The camera cache hands back the same File for its whole TTL - only reclaim a distinct one.
        if (image != null && image != keeper.cameraSnapshot.image) {
            removeFile(image, logger)
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
        private val REFRESH_TIMEOUT = 60.seconds

        // Must exceed RoadConditionsFetcherImpl's 15s jsoup timeout.
        private val COLD_START_TIMEOUT = 20.seconds
    }
}
