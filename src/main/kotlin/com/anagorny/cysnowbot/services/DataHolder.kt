package com.anagorny.cysnowbot.services

import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer

interface DataHolder {
    fun getRoadConditions() : RoadConditionsContainer
    fun getCameraSnapshot() : CameraSnapshotContainer
}
