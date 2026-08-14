package com.anagorny.cysnowbot.handlers

import com.anagorny.cysnowbot.helpers.withErrorLogging
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class OtherMessageHandler(
    private val telegramClient: TelegramClient
) : UpdatesHandler {
    override suspend fun handle(update: Update) = withContext(MDCContext()) {
        val message = update.message
        if (message.chat.isUserChat) {
            logger.info { "Unknown message id=${message.messageId}, text: '${message.text}'" }
            withErrorLogging(
                { msg, e -> logger.error(e) { msg } },
                "Error while replying to message=${message.messageId} from user='${message.from.userName}' in the chat='{${message.chat.title}}'"
            ) {
                telegramClient.execute(
                    SendMessage.builder()
                        .replyToMessageId(message.messageId)
                        .chatId(message.chatId.toString())
                        .text("Unknown command")
                        .parseMode("HTML")
                        .build()
                )
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
