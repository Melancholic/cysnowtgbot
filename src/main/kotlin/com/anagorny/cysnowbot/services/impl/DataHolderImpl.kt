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

            try {
                val roadConditionsResult = roadConditionsResultDef.await() ?: RoadConditionsContainer()
                if (roadConditionsContainer == null) {
                    roadConditionsContainer = AtomicReference(roadConditionsResult)
                } else {
                    roadConditionsContainer?.updateAndGet { roadConditionsResult }
                }
            } catch (e: Exception) {
                logger.error { "Error while updating state of road conditions" }
                roadConditionsContainer = null
            }

            try {
                val cameraSnapshotResult = cameraSnapshotResultDef.await() ?: CameraSnapshotContainer()
                updateCameraSnapshotContainer(cameraSnapshotResult)
            } catch (e: Exception) {
                logger.error { "Error while updating state of live camera snapshot" }
                updateCameraSnapshotContainer(null)
                cameraSnapshotContainer = null
            }

            logger.info { "Updating state completed" }
        }
    }

    private fun updateCameraSnapshotContainer(newState: CameraSnapshotContainer?) {
        if (cameraSnapshotContainer?.get() != null) {
            removeFile(cameraSnapshotContainer?.get()?.image, logger)
        }
        if (cameraSnapshotContainer != null) {
            cameraSnapshotContainer!!.updateAndGet { newState }
        } else {
            cameraSnapshotContainer = AtomicReference(newState)
        }
    }

    companion object : KLogging()
}
