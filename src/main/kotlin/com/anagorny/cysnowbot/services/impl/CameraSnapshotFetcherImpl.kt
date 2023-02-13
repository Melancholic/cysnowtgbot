package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.runAsync
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.services.CameraSnapshotFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import mu.KLogging
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File
import java.lang.String.format
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.*


@Service
class CameraSnapshotFetcherImpl(
    @Qualifier("mainFlowCoroutineScope")
    private val scope: CoroutineScope,
    @Value("\${live-camera.stream-page-url}") val liveStreamPageUrl: String,
    @Value("\${live-camera.snapshot-url-template}") val snapshotUrlTemplate: String,
    @Value("\${live-camera.timeouts.connect}") val connectTimeout: Duration,
    @Value("\${live-camera.timeouts.read}") val readTimeout: Duration
) : CameraSnapshotFetcher {
    private val restTemplate = RestTemplateBuilder()
        .setReadTimeout(readTimeout)
        .setConnectTimeout(connectTimeout)
        .build()

    override suspend fun fetchCameraSnapshot(): Deferred<CameraSnapshotContainer?> {
        return scope.runAsync {
            val imageBytes: ByteArray? = tryDownloadSnapshotImage()
            if (imageBytes == null) {
                null
            } else {
                val file = File.createTempFile("camera-snapshot", ".jpg")
                Files.write(file.toPath(), imageBytes)
                CameraSnapshotContainer(file)
            }
        }
    }

    private fun tryDownloadSnapshotImage(): ByteArray? {
        if (streamId == null) streamId = retrieveStreamId(liveStreamPageUrl)
        var response = downloadImage(format(snapshotUrlTemplate, streamId))
        if (response == null) {
            streamId = retrieveStreamId(liveStreamPageUrl)
            response = downloadImage(format(snapshotUrlTemplate, streamId))
        }
        return response
    }

    private fun downloadImage(url: String): ByteArray? = try {
        val entity: ResponseEntity<ByteArray> = restTemplate.getForEntity(url, ByteArray::class.java)
        if (entity.statusCode == HttpStatus.OK) {
            entity.body
        } else {
            logger.error("Error downloading file from '$url', response status: ${entity.statusCodeValue}")
            null
        }
    } catch (e: java.lang.Exception) {
        logger.error("Error downloading file from '$url'", e)
        null
    }

    protected fun retrieveStreamId(urlToLiveStream: String): String? {
        val html: String? = Jsoup.connect(urlToLiveStream)?.get()?.html()
        val streamId = html?.let { "var streamid = '(.+)';".toRegex().find(it)?.groups?.get(1)?.value }
        logger.info { "Fetched streamId=${streamId} for Trodos live camera." }
        return streamId
    }

    companion object : KLogging() {
        var streamId: String? = null
    }

}
