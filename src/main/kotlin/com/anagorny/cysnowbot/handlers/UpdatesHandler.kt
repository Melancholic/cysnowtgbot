package com.anagorny.cysnowbot.handlers

import org.telegram.telegrambots.meta.api.objects.Update

interface UpdatesHandler {
    suspend fun handle(update: Update)
}
