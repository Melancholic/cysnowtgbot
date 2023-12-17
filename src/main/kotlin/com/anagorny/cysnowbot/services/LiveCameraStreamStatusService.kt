package com.anagorny.cysnowbot.services

import com.anagorny.cysnowbot.models.CameraStatus

interface LiveCameraStreamStatusService {
    suspend fun cameraStreamStatus(): CameraStatus
}
