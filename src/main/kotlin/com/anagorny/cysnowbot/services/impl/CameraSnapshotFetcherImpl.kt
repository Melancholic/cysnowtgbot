package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.services.Fetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import mu.KLogging
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
    @Value("\${live-camera.stream-status.url}") private val baseUrl: String,
    restTemplateBuilder: RestTemplateBuilder
) : Fetcher<CameraSnapshotContainer> {
    private val restTemplate = restTemplateBuilder.build()
    private val url = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .toUriString()

    override fun fetchAsFlow(): Flow<CameraSnapshotContainer?> {
        return flow {
            val imageBytes: ByteArray? = downloadImage(url)
            if (imageBytes == null) {
                emit(null)
            } else {
                val file = withContext(Dispatchers.IO) {
                    File.createTempFile("camera-snapshot", ".jpg")
                }
                withContext(Dispatchers.IO) {
                    Files.write(file.toPath(), imageBytes)
                }
                logger.info { "Live camera snapshot successfully saved to '${file.absolutePath}" }
                emit(CameraSnapshotContainer(file))
            }
        }.catch { e ->
            logger.error(e) { "Error while updating state of live camera snapshot" }
            emit(null)
        }
    }

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
