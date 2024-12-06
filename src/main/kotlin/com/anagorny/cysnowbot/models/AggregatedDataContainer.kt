package com.anagorny.cysnowbot.models

import java.time.LocalDateTime

data class AggregatedDataContainer(
    val roadConditions: RoadConditionsContainer = RoadConditionsContainer(),
    val cameraSnapshot: CameraSnapshotContainer = CameraSnapshotContainer(),
    val olympusWeatherStatus: WeatherStatus? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun builder(): AggregatedDataContainerBuilder {
            return AggregatedDataContainerBuilder()
        }
    }

    class AggregatedDataContainerBuilder(
        private var roadConditions: RoadConditionsContainer = RoadConditionsContainer(),
        private var cameraSnapshot: CameraSnapshotContainer = CameraSnapshotContainer(),
        private var olympusWeatherStatus: WeatherStatus? = null
    ) {

        fun roadConditions(roadConditions: RoadConditionsContainer?): AggregatedDataContainerBuilder {
            roadConditions?.let{this.roadConditions = it }
            return this
        }

        fun cameraSnapshot(cameraSnapshot: CameraSnapshotContainer?): AggregatedDataContainerBuilder {
            cameraSnapshot?.let{this.cameraSnapshot = it }
            return this
        }

        fun olympusWeatherStatus(olympusWeatherStatus: WeatherStatus?): AggregatedDataContainerBuilder {
            olympusWeatherStatus?.let { this.olympusWeatherStatus = it }
            return this;
        }

        fun build(): AggregatedDataContainer = AggregatedDataContainer(roadConditions, cameraSnapshot, olympusWeatherStatus)

    }
}
