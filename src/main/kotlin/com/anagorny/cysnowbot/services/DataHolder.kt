package com.anagorny.cysnowbot.services

import com.anagorny.cysnowbot.models.AggregatedDataContainer

interface DataHolder {
    fun getData(): AggregatedDataContainer
}
