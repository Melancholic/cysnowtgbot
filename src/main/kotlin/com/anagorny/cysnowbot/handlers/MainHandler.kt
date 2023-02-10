package com.anagorny.cysnowbot.handlers

import com.anagorny.cysnowbot.clients.RoadConditionsClient
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.services.MainTelegramBotService
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class MainHandler(
    private val roadConditionsClient: RoadConditionsClient,
    private val botService: MainTelegramBotService
) : UpdatesHandler {

    override suspend fun handle(update: Update) = withContext(MDCContext()) {
        val message = update.message

        val roadConditions = roadConditionsClient.fetchRoadConditions()
        if (roadConditions.isPresent) {
            botService.execute(buildResponse(message, roadConditions.get()))
        }
        logger.info("Unknown message id=${message.messageId}, text: '${message.text}'")
        botService.execute(DeleteMessage().apply {
            chatId = message.chatId.toString()
            messageId = message.messageId
        })
        logger.info("Message with id=${message.messageId} was deleted")
    }

    private fun buildResponse(message: Message, roadConditions: RoadConditionsContainer) =
        SendMessage().apply {
            chatId = message.chatId.toString()
            replyToMessageId = message.messageId
            text = makeCaption(roadConditions)
        }


    fun makeCaption(roadConditions: RoadConditionsContainer): String = roadConditions.toString()
    companion object : KLogging()
}
