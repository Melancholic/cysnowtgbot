package com.anagorny.cysnowbot.clients.impl

import com.anagorny.cysnowbot.clients.CameraSnapshotFetcher
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import mu.KLogging
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.io.File
import java.net.URL
import java.time.Duration
import java.util.*

@Service
class CameraSnapshotFetcherImpl(
    @Value("\${camera-snapshot.url}") val cameraSnapshotUrl: String,
    @Value("\${camera-snapshot.timeouts.connect}") val connectTimeout: Duration,
    @Value("\${camera-snapshot.timeouts.read}") val readTimeout: Duration
) : CameraSnapshotFetcher {

    @Cacheable(value = ["camera-snapshots-cache"])
    override fun fetchCameraSnapshot(): Optional<CameraSnapshotContainer> {
        val url = URL(cameraSnapshotUrl)
        val file = File.createTempFile("camera-snapshot", ".jpg")
        return try {
            FileUtils.copyURLToFile(
                url,
                file,
                connectTimeout.toMillis().toInt(),
                readTimeout.toMillis().toInt(),
            )
            return Optional.of(CameraSnapshotContainer(file))
        } catch (e: Exception) {
            logger.error("Error downloading file from '$cameraSnapshotUrl'", e)
            Optional.empty()
        }
    }

    companion object : KLogging()

}
