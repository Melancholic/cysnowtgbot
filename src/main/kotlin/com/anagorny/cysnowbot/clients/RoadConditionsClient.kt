package com.anagorny.cysnowbot.clients

import com.anagorny.cysnowbot.models.RoadConditionsContainer
import java.util.*

interface RoadConditionsClient {
    fun fetchRoadConditions(): Optional<RoadConditionsContainer>
}
