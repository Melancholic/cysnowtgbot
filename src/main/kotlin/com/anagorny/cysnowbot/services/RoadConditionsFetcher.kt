package com.anagorny.cysnowbot.services

import com.anagorny.cysnowbot.models.RoadConditionsContainer
import kotlinx.coroutines.Deferred

interface RoadConditionsFetcher {
    suspend fun fetchRoadConditions(): Deferred<RoadConditionsContainer?>
}
