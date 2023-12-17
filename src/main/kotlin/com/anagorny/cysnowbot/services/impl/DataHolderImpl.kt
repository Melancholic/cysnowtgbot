package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.removeFile
import com.anagorny.cysnowbot.models.AggregatedDataContainer
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.services.DataHolder
import com.anagorny.cysnowbot.services.Fetcher
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class DataHolderImpl(
    private val cameraSnapshotFetcher: Fetcher<CameraSnapshotContainer>,
    private val roadConditionsFetcher: Fetcher<RoadConditionsContainer>,
) : DataHolder {
    private val schedulerInterval = Duration.ofMinutes(UPDATE_INTERVAL_IN_MINUTES)
    private val aggregatedData = AtomicReference<AggregatedDataContainer?>();
    private val locker = ReentrantLock()

    override fun getData(): AggregatedDataContainer {
        if (aggregatedData.get() == null) {
            updateState()
        }
        return aggregatedData.get() ?: AggregatedDataContainer()
    }

    @Scheduled(fixedDelay = UPDATE_INTERVAL_IN_MINUTES, timeUnit = TimeUnit.MINUTES)
    protected fun updateState() {
        locker.withLock {
            if (Duration.between(
                    aggregatedData.get()?.timestamp ?: LocalDateTime.MIN,
                    LocalDateTime.now()
                ) >= schedulerInterval
            ) {
                logger.info { "Updating state running" }
                runBlocking {
                    val roadConditionsResultDef = roadConditionsFetcher.fetchAsync()
                    val cameraSnapshotResultDef = cameraSnapshotFetcher.fetchAsync()

                    val result = AggregatedDataContainer.builder()

                    try {
                        result.roadConditions(roadConditionsResultDef.await())
                    } catch (e: Exception) {
                        logger.error(e) { "Error while updating state of road conditions" }
                    }

                    try {
                        result.cameraSnapshot(cameraSnapshotResultDef.await())
                    } catch (e: Exception) {
                        logger.error(e) { "Error while updating state of live camera snapshot" }
                    }

                    doCleanup(aggregatedData.getAndUpdate { result.build() })
                }
                logger.info { "Updating state completed" }
            } else {
                logger.info { "The state has been updated recently, no update is needed now" }
            }
        }
    }

    private fun doCleanup(old: AggregatedDataContainer?) {
        old?.cameraSnapshot?.let {
            removeFile(it.image, logger)
        }
    }

    companion object : KLogging() {
        private const val UPDATE_INTERVAL_IN_MINUTES: Long = 3
    }
}
