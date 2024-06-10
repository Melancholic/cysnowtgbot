package com.anagorny.cysnowbot.handlers

import com.anagorny.cysnowbot.helpers.withErrorLogging
import com.anagorny.cysnowbot.services.MainTelegramBotService
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class OtherMessageHandler(
    private val botService: MainTelegramBotService
) : UpdatesHandler {
    override suspend fun handle(update: Update) = withContext(MDCContext()) {
        val message = update.message
        if (message.chat.isUserChat) {
            logger.info("Unknown message id=${message.messageId}, text: '${message.text}'")
            withErrorLogging(
                logger::error,
                "Error while replying to message=${message.messageId} from user='${message.from.userName}' in the chat='{${message.chat.title}}'"
            ) {
                botService.execute(SendMessage().apply {
                    replyToMessageId = message.messageId
                    chatId = message.chatId.toString()
                    text = "Unknown command"
                    parseMode = "HTML"
                })
            }
        }
    }

    companion object : KLogging()
}
