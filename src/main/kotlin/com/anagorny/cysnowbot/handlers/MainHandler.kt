package com.anagorny.cysnowbot.handlers

import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class MainHandler(
    private val otherMessageHandler: UpdatesHandler
) : UpdatesHandler {

    override suspend fun handle(update: Update) = withContext(MDCContext()) {
        if (update.hasMessage()) {
            val message = update.message
            logger.info { "Got message with id=${message.messageId} in chat ${message.chat.id} from ${message.from.userName}(${message.from.id})" }
            launch {
                otherMessageHandler.handle(update)
            }
        }
    }

    companion object : KLogging()
}
