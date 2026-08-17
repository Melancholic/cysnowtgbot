package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.config.CachingConfig.Companion.CAMERA_SNAPSHOT_CACHE_NAME
import com.anagorny.cysnowbot.helpers.io
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.services.Fetcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.nio.file.Files


@Service
class CameraSnapshotFetcherImpl(
    @Value("\${live-camera.stream-status.url}") private val baseUrl: String,
    restTemplateBuilder: RestTemplateBuilder
) : Fetcher<CameraSnapshotContainer> {
    private val restTemplate = restTemplateBuilder.build()
    private val url = UriComponentsBuilder.fromUriString(baseUrl)
        .toUriString()

    @Cacheable(CAMERA_SNAPSHOT_CACHE_NAME, sync = true)
    override fun fetchAsFlow() = flow {
            val file = io {
                val imageBytes = downloadImage(url)
                File.createTempFile("camera-snapshot", ".jpg").also { Files.write(it.toPath(), imageBytes) }
            }
            logger.info { "Live camera snapshot successfully saved to '${file.absolutePath}'" }
            emit(CameraSnapshotContainer(file))
        }


    private fun downloadImage(url: String): ByteArray {
        val entity: ResponseEntity<ByteArray> = restTemplate.getForEntity(url, ByteArray::class.java)
        check(entity.statusCode == HttpStatus.OK) {
            "Error downloading file from '$url', response status: ${entity.statusCode}"
        }
        return checkNotNull(entity.body) { "Empty body while downloading file from '$url'" }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
