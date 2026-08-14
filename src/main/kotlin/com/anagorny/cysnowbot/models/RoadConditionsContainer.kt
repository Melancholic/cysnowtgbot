package com.anagorny.cysnowbot.models

import io.github.oshai.kotlinlogging.KotlinLogging
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
        private val logger = KotlinLogging.logger {}

        // "4x4/chains" must be checked before "open" - its text also contains "or".
        fun parseFromText(msg: String?): RoadStatus {
            val normalized = msg?.trim()?.lowercase()
            if (normalized.isNullOrEmpty()) return UNKNOWN
            return when {
                "closed" in normalized -> CLOSED
                "4x4" in normalized || "4×4" in normalized || "chain" in normalized -> AWD_OR_CHAINS_ONLY
                "open" in normalized -> OPEN
                else -> UNKNOWN.also { logger.warn { "Unrecognized road status text: '$msg'" } }
            }
        }
    }
}
