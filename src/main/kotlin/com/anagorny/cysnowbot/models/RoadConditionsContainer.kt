package com.anagorny.cysnowbot.models

import java.time.LocalDateTime

data class RoadConditionsContainer(
    val roads : List<RoadStateContainer>,
    val updatedAt: LocalDateTime?,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "RoadConditionsContainer(roads=$roads, updatedAt=$updatedAt, timestamp=$timestamp)"
    }
}

data class RoadStateContainer(
    val src: String?,
    val dst: String?,
    val roadStatus: String?
) {
    override fun toString(): String {
        return "RoadStateContainer(src=$src, dst=$dst, roadStatus=$roadStatus)"
    }
}
