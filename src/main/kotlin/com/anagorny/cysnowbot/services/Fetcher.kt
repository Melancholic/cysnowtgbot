package com.anagorny.cysnowbot.services

import kotlinx.coroutines.flow.Flow

interface Fetcher<T> {
    fun fetchAsFlow(): Flow<T?>
}
