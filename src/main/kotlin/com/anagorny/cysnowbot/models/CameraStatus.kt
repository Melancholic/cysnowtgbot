package com.anagorny.cysnowbot.models

import java.time.LocalDateTime

data class CameraStatus(
    val streamUrl: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now()
)
