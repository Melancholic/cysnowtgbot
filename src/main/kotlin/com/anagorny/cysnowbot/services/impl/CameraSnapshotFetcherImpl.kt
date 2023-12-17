package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.runAsync
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.CameraStatus
import com.anagorny.cysnowbot.services.Fetcher
import com.anagorny.cysnowbot.services.LiveCameraStreamStatusService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.nio.file.Files
import java.util.*


@Service
class CameraSnapshotFetcherImpl(
    @Qualifier("mainFlowCoroutineScope")
    private val scope: CoroutineScope,
    @Value("\${live-camera.alias}") val cameraAlias: String,
    val cameraStatusService: LiveCameraStreamStatusService,
    restTemplateBuilder: RestTemplateBuilder
) : Fetcher<CameraSnapshotContainer> {
    private val restTemplate = restTemplateBuilder.build()

    override suspend fun fetchAsync(): Deferred<CameraSnapshotContainer?> {
        return scope.runAsync {
            val cameraStatus = cameraStatusService.cameraStreamStatus()
            return@runAsync if (cameraStatus.streamIsAvailable) {
                val imageBytes: ByteArray? =
                    downloadImage(buildSnapshotUrl(cameraStatus), cameraAlias)
                if (imageBytes == null) {
                    null
                } else {
                    val file = withContext(Dispatchers.IO) {
                        File.createTempFile("camera-snapshot", ".jpg")
                    }
                    withContext(Dispatchers.IO) {
                        Files.write(file.toPath(), imageBytes)
                    }
                    logger.info { "Live camera snapshot successfully saved to '${file.absolutePath}" }
                    CameraSnapshotContainer(file)
                }
            } else {
                logger.warn { "Live camera stream is not available now" }
                null
            }
        }
    }

    private fun buildSnapshotUrl(cameraStatus: CameraStatus) = UriComponentsBuilder.fromHttpUrl(cameraStatus.streamUrl)
        .pathSegment("streams")
        .pathSegment(cameraStatus.streamId)
        .pathSegment("snapshot.jpg")
        .queryParam("alias", cameraAlias)
        .toUriString()

    private fun downloadImage(url: String, alias: String): ByteArray? = try {
        val queryParams = hashMapOf("alias" to alias)
        val entity: ResponseEntity<ByteArray> = restTemplate.getForEntity(url, ByteArray::class.java, queryParams)
        if (entity.statusCode == HttpStatus.OK) {
            entity.body
        } else {
            logger.error("Error downloading file from '$url', response status: ${entity.statusCode}")
            null
        }
    } catch (e: java.lang.Exception) {
        logger.error("Error downloading file from '$url'", e)
        null
    }

    companion object : KLogging()
}
