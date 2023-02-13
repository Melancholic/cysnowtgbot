package com.anagorny.cysnowbot.services

import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import kotlinx.coroutines.Deferred

interface CameraSnapshotFetcher {
    suspend fun fetchCameraSnapshot(): Deferred<CameraSnapshotContainer?>
}
