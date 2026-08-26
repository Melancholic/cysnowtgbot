package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.config.SystemProperties
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
import kotlinx.coroutines.time.withTimeoutOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException

@Service
class DataHolderImpl(
    private val cameraSnapshotFetcher: Fetcher<CameraSnapshotContainer>,
    private val roadConditionsFetcher: Fetcher<RoadConditionsContainer>,
    private val olympusWeatherStatusFetcher: Fetcher<WeatherStatus>,
    @Qualifier("mainFlowCoroutineScope") private val scope: CoroutineScope,
    private val systemProperties: SystemProperties
) : DataHolder {
    private val aggregatedData = AtomicReference<AggregatedDataContainer?>()

    @Volatile
    private var updateJob: Job? = null

    override fun getData(): AggregatedDataContainer {
        if (isAbsent() || isOutdated()) {
            val job = launchAsyncRefresh()
            runBlocking { withTimeoutOrNull(systemProperties.dataHolder.waitTimeout) { job.join() } }
        }
        return aggregatedData.get() ?: AggregatedDataContainer()
    }

    @Synchronized
    private fun launchAsyncRefresh(): Job =
        updateJob?.takeIf { it.isActive } ?: scope.launch { updateState() }.also { updateJob = it }

    private fun isOutdated() = Duration.between(
        aggregatedData.get()?.timestamp ?: LocalDateTime.MIN,
        LocalDateTime.now()
    ) >= systemProperties.dataHolder.maxDataAge

    private fun isAbsent() = aggregatedData.get() == null

    protected suspend fun updateState() {
        logger.info { "Updating state starting" }

        val camera = scope.runAsync { cameraSnapshotFetcher.fetchOrNull() }
        val roads = scope.runAsync { roadConditionsFetcher.fetchOrNull() }
        val weather = scope.runAsync { olympusWeatherStatusFetcher.fetchOrNull() }

        val result = AggregatedDataContainer.builder(aggregatedData.get())
            .roadConditions(roads.await())
            .cameraSnapshot(camera.await())
            .olympusWeatherStatus(weather.await())
            .build()
        doApplyNewState(result)
        logger.info { "Updating state completed" }
    }

    private suspend fun doApplyNewState(newState: AggregatedDataContainer?) {
        val oldState = aggregatedData.getAndSet(newState)
        oldState?.cameraSnapshot?.image?.let { oldImage ->
            if (newState?.cameraSnapshot?.image?.path != oldImage.path) {
                withContext(Dispatchers.IO) {
                    removeFile(oldImage, logger)
                }
            }
        }
    }

    private suspend fun <T : Any> Fetcher<T>.fetchOrNull(): T? = try {
        this.fetchAsFlow().firstOrNull()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.error(e) { "Fetch failed: ${this::class.simpleName}" }
        null
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
