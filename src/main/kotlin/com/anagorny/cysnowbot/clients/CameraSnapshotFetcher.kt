package com.anagorny.cysnowbot.clients

import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import java.util.*

interface CameraSnapshotFetcher {
    fun fetchCameraSnapshot(): Optional<CameraSnapshotContainer>
}
