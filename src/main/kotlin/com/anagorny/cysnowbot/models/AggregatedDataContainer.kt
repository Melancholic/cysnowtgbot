package com.anagorny.cysnowbot.models

import java.time.LocalDateTime

data class AggregatedDataContainer(
    val roadConditions: RoadConditionsContainer = RoadConditionsContainer(),
    val cameraSnapshot: CameraSnapshotContainer = CameraSnapshotContainer(),
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun builder(): AggregatedDataContainerBuilder {
            return AggregatedDataContainerBuilder()
        }
    }

    class AggregatedDataContainerBuilder(
        private var roadConditions: RoadConditionsContainer = RoadConditionsContainer(),
        private var cameraSnapshot: CameraSnapshotContainer = CameraSnapshotContainer()
    ) {

        fun roadConditions(roadConditions: RoadConditionsContainer?): AggregatedDataContainerBuilder {
            roadConditions?.let{this.roadConditions = it }
            return this
        }

        fun cameraSnapshot(cameraSnapshot: CameraSnapshotContainer?): AggregatedDataContainerBuilder {
            cameraSnapshot?.let{this.cameraSnapshot = it }
            return this
        }

        fun build(): AggregatedDataContainer = AggregatedDataContainer(roadConditions, cameraSnapshot)

    }
}
