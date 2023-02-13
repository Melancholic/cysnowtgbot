package com.anagorny.cysnowbot.models

import java.io.File
import java.time.LocalDateTime

data class CameraSnapshotContainer(
    val image: File? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
