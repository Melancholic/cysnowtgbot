package com.anagorny.cysnowbot.services.impl

import com.anagorny.cysnowbot.helpers.runAsync
import com.anagorny.cysnowbot.models.CameraStatus
import com.anagorny.cysnowbot.services.LiveCameraStreamStatusService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference

@Service
class LiveCameraStreamStatusServiceImpl(
    @Qualifier("mainFlowCoroutineScope")
    private val scope: CoroutineScope,
    @Value("\${live-camera.alias}") private val cameraAlias: String,
    @Value("\${live-camera.stream-status.update-interval:1h}") private val updateInterval: Duration,
    @Value("\${live-camera.stream-status.url}") private val baseUrl: String,
    private val mapper: ObjectMapper,
    restTemplateBuilder: RestTemplateBuilder
) : LiveCameraStreamStatusService {
    private val url = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .queryParam("alias", cameraAlias)
        .toUriString()

    private val restTemplate = restTemplateBuilder.build()

    private val cameraStatus = AtomicReference<CameraStatus>()

    override suspend fun cameraStreamStatus(): CameraStatus {
        if (isStateNeedToUpdate()) {
            updateStatus()
        }
        return cameraStatus.get()
    }

    private suspend fun updateStatus() {
        val status = getStatusAsync().await()
        cameraStatus.set(status)
    }

    private suspend fun getStatusAsync(): Deferred<CameraStatus> {
        return scope.runAsync {
            val response = withContext(Dispatchers.IO) {
                restTemplate.getForEntity(url, String::class.java)
            }

            if (response.statusCode.is2xxSuccessful) {
                /*
                * This is required just because the camera's provider returns json with
                * `Content-Type: text/html; charset=UTF-8`
                */
                val body = mapper.readTree(response.body)?.get("details")
                val streamIsAvailable = body?.get("streamavailable")?.asInt()?.let { it == 1 }
                val streamId = body?.get("streamid")?.asText()
                val streamUrl = body?.get("address")?.asText()
                if (streamIsAvailable == null) {
                    logger.warn { "Couldn't parse camera's stream status response: `streamavailable` property not found in response" }
                }

                if (streamId == null) {
                    logger.warn { "Couldn't parse camera's stream status response: `streamid` property not found in response" }
                }

                if (streamUrl == null) {
                    logger.warn { "Couldn't parse camera's stream status response: `address` property not found in response" }
                }
                return@runAsync if (streamIsAvailable == null
                    || streamId == null
                    || streamUrl == null
                    || !streamIsAvailable
                ) {
                    CameraStatus()
                } else {
                    CameraStatus(streamIsAvailable, streamId, streamUrl)
                }
            } else if (response.statusCode.is4xxClientError) {
                throw HttpClientErrorException(response.statusCode)
            } else {
                throw HttpServerErrorException(response.statusCode)
            }
        }
    }

    private fun isStateNeedToUpdate(): Boolean {
        return cameraStatus.get()
            ?.let { Duration.between(it.timestamp, LocalDateTime.now()) > updateInterval }
            ?: true
    }

    companion object : KLogging()
}
