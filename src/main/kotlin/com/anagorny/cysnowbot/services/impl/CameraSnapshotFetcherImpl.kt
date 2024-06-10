package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.CameraStatus
import com.anagorny.cysnowbot.services.Fetcher
import com.anagorny.cysnowbot.services.LiveCameraStreamStatusService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    @Qualifier("mainFlowCoroutineScope") private val scope: CoroutineScope,
    @Value("\${live-camera.alias}") val cameraAlias: String,
    val cameraStatusService: LiveCameraStreamStatusService,
    restTemplateBuilder: RestTemplateBuilder
) : Fetcher<CameraSnapshotContainer> {
    private val restTemplate = restTemplateBuilder.build()

    override fun fetchAsFlow(): Flow<CameraSnapshotContainer?> {
        return flow {
            emit(cameraStatusService.cameraStreamStatus())
        }.map { cameraStatus ->
            if (cameraStatus.streamIsAvailable) {
                val imageBytes: ByteArray? = downloadImage(buildSnapshotUrl(cameraStatus))
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
        }.catch { e ->
            logger.error(e) { "Error while updating state of live camera snapshot" }
            emit(null)
        }
    }


    private fun buildSnapshotUrl(cameraStatus: CameraStatus) =
        UriComponentsBuilder.fromHttpUrl(cameraStatus.streamUrl).pathSegment("streams")
            .pathSegment(cameraStatus.streamId).pathSegment("snapshot.jpg").queryParam("alias", cameraAlias)
            .toUriString()

    private fun downloadImage(url: String): ByteArray? = try {
        val entity: ResponseEntity<ByteArray> = restTemplate.getForEntity(url, ByteArray::class.java)
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
