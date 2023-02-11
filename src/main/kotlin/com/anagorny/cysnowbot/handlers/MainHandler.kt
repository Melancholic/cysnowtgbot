package com.anagorny.cysnowbot.handlers

import com.anagorny.cysnowbot.clients.CameraSnapshotFetcher
import com.anagorny.cysnowbot.clients.RoadConditionsClient
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.services.MainTelegramBotService
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Component
class MainHandler(
    private val roadConditionsClient: RoadConditionsClient,
    private val cameraSnapshotFetcher: CameraSnapshotFetcher,
    private val botService: MainTelegramBotService
) : UpdatesHandler {

    override suspend fun handle(update: Update) = withContext(MDCContext()) {
        val message = update.message

        val roadConditions = roadConditionsClient.fetchRoadConditions()
        val cameraSnapshot = cameraSnapshotFetcher.fetchCameraSnapshot()
        if (roadConditions.isPresent) {
            botService.execute(buildResponse(message, roadConditions.get(), cameraSnapshot.get()))
        }
        logger.info("Unknown message id=${message.messageId}, text: '${message.text}'")
        botService.execute(DeleteMessage().apply {
            chatId = message.chatId.toString()
            messageId = message.messageId
        })
        logger.info("Message with id=${message.messageId} was deleted")
    }

    private fun buildResponse(message: Message, roadConditions: RoadConditionsContainer, cameraSnapshot: CameraSnapshotContainer?) =
        SendPhoto().apply {
            photo = InputFile(cameraSnapshot?.image)
            chatId = message.chatId.toString()
            replyToMessageId = message.messageId
            caption = makeCaption(roadConditions)
            parseMode = "HTML"
        }

    fun makeCaption(roadConditions: RoadConditionsContainer): String = buildString {
        var titleSuffix = roadConditions.updatedAt?.let{" <i>(of ${it.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))})</i>"} ?:""
        append("<b>Cyprus Road Conditions${titleSuffix}</b>")
        append("\n\n")
        roadConditions.roads.forEach {
            append("${it.roadStatus?.icon?:""} <b>${it.src} - ${it.dst}</b> <i>(${it.roadStatus?.message})</i>").append("\n")
        }
    }
    companion object : KLogging()
}
