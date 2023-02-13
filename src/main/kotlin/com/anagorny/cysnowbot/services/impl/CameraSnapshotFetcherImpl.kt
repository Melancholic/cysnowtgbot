package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.runAsync
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.services.CameraSnapshotFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import mu.KLogging
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.io.File
import java.lang.String.format
import java.nio.file.Files
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
                logger.info{ "Live camera snapshot successfully saved to '${file.absolutePath}"}
                CameraSnapshotContainer(file)
            }
        }
    }

    private fun tryDownloadSnapshotImage(): ByteArray? {
        var response = downloadImage(
            format(
                snapshotUrlTemplate, host ?: retrieveHost(liveStreamPageUrl), streamId
                    ?: retrieveStreamId(liveStreamPageUrl)
            )
        )
        if (response == null) {
            host = retrieveHost(liveStreamPageUrl)
                ?: throw IllegalStateException("Couldn't fetch a live camera snapshot host")
            streamId = retrieveStreamId(liveStreamPageUrl)
                ?: throw IllegalStateException("Couldn't fetch a live camera stream id")
            response = downloadImage(format(snapshotUrlTemplate, host, streamId))
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
        logger.info { "Fetched streamId=${streamId} for Trodos live camera snapshot." }
        return streamId
    }

    protected fun retrieveHost(urlToLiveStream: String): String? {
        val html: String? = Jsoup.connect(urlToLiveStream)?.get()?.html()
        val host = html?.let { "var address = '(.+)';".toRegex().find(it)?.groups?.get(1)?.value }
        logger.info { "Fetched host=${host} for Trodos live camera snapshot." }
        return host
    }

    companion object : KLogging() {
        var streamId: String? = null
        var host: String? = null
    }

}
