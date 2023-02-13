package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.removeFile
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.services.CameraSnapshotFetcher
import com.anagorny.cysnowbot.services.DataHolder
import com.anagorny.cysnowbot.services.RoadConditionsFetcher
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Service
class DataHolderImpl(
    private val cameraSnapshotFetcher: CameraSnapshotFetcher,
    private val roadConditionsFetcher: RoadConditionsFetcher
) : DataHolder {
    private var cameraSnapshotContainer: AtomicReference<CameraSnapshotContainer>? = null
    private var roadConditionsContainer: AtomicReference<RoadConditionsContainer>? = null

    override fun getRoadConditions(): RoadConditionsContainer {
        if (roadConditionsContainer == null) {
            updateState()
        }
        return roadConditionsContainer?.get() ?: RoadConditionsContainer()
    }

    override fun getCameraSnapshot(): CameraSnapshotContainer {
        if (cameraSnapshotContainer == null) {
            updateState()
        }
        return cameraSnapshotContainer?.get() ?: CameraSnapshotContainer()
    }

    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.MINUTES)
    protected fun updateState() {
        runBlocking {
            logger.info { "Updating state running" }
            val roadConditionsResultDef = roadConditionsFetcher.fetchRoadConditions()
            val cameraSnapshotResultDef = cameraSnapshotFetcher.fetchCameraSnapshot()

            val roadConditionsResult = roadConditionsResultDef.await() ?: RoadConditionsContainer()
            if (roadConditionsContainer == null) {
                roadConditionsContainer = AtomicReference(roadConditionsResult)
            } else {
                roadConditionsContainer?.updateAndGet { roadConditionsResult }
            }

            val cameraSnapshotResult = cameraSnapshotResultDef.await() ?: CameraSnapshotContainer()
            if (cameraSnapshotContainer == null) {
                cameraSnapshotContainer = AtomicReference(cameraSnapshotResult)
            } else {
                cameraSnapshotContainer?.updateAndGet {
                    it.image?.let { oldFile -> removeFile(oldFile, logger) }
                    cameraSnapshotResult
                }
            }

            logger.info { "Updating state completed" }
        }
    }

    companion object : KLogging()
}
