package com.anagorny.cysnowbot.models

import org.apache.commons.lang3.StringUtils
import java.time.LocalDateTime

data class RoadConditionsContainer(
    val roads: List<RoadStateContainer> = emptyList(),
    val updatedAt: LocalDateTime? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "RoadConditionsContainer(roads=$roads, updatedAt=$updatedAt, timestamp=$timestamp)"
    }
}

data class RoadStateContainer(
    val src: String?,
    val dst: String?,
    val roadStatus: RoadStatus?
) {
    override fun toString(): String {
        return "RoadStateContainer(src=$src, dst=$dst, roadStatus=$roadStatus)"
    }
}

enum class RoadStatus(
    val message: String,
    val icon: String
) {
    OPEN("OPEN for all Vehicles", "✅"),
    CLOSED("CLOSED", "⛔️"),
    AWD_OR_CHAINS_ONLY("4X4 or car with chains", "⚠️"),
    UNKNOWN("Unknown", "❔");


    companion object {
        fun parseFromText(msg: String?): RoadStatus =
            values().asSequence()
                .find { StringUtils.equalsAnyIgnoreCase(it.message, msg) }
                ?: UNKNOWN
    }
}
