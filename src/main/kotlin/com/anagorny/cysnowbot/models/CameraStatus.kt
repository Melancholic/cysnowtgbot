package com.anagorny.cysnowbot.models

import java.time.LocalDateTime

data class CameraStatus(
    val streamIsAvailable: Boolean = false,
    val streamId: String = "",
    val streamUrl: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now()
)
