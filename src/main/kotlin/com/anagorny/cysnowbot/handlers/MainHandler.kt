package com.anagorny.cysnowbot.handlers

import com.anagorny.cysnowbot.services.MainTelegramBotService
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class MainHandler(
    private val botService: MainTelegramBotService,
) : UpdatesHandler {

    override suspend fun handle(update: Update) = withContext(MDCContext()) {
        val message = update.message
        logger.info("Unknown message id=${message.messageId}, text: '${message.text}'")
        botService.execute(DeleteMessage().apply {
            chatId = message.chatId.toString()
            messageId = message.messageId
        })
        logger.info("Message with id=${message.messageId} was deleted")
    }

    companion object : KLogging()
}
