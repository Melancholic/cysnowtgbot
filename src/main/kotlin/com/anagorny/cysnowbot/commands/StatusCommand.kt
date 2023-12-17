package com.anagorny.cysnowbot.commands

import com.anagorny.cysnowbot.helpers.withErrorLogging
import com.anagorny.cysnowbot.models.AggregatedDataContainer
import com.anagorny.cysnowbot.models.CameraSnapshotContainer
import com.anagorny.cysnowbot.models.RoadConditionsContainer
import com.anagorny.cysnowbot.services.DataHolder
import com.anagorny.cysnowbot.services.RateLimiter
import mu.KLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Component
class StatusCommand(
    private val dataHolder: DataHolder,
    rateLimiter: RateLimiter
) : RateLimitedCommand(
    "get_road_status",
    "Returns the latest roads conditions with capture from live camera",
    rateLimiter
) {

    override fun doExecute(sender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        sentAction(sender, chat, ActionType.TYPING)
        val data = dataHolder.getData()

        if (data.roadConditions.roads.isEmpty()) {
            doError(ResponseErrorsType.NOT_PRESENT_ERR, sender, user, chat)
        } else {
            doResponse(sender, user, chat, data)
        }
    }

    private fun doError(
        error: ResponseErrorsType = ResponseErrorsType.ERROR,
        sender: AbsSender,
        user: User,
        chat: Chat
    ) {
        withErrorLogging(
            logger::error,
            "Error while processing command from user='${user.userName}' to chat='{${chat.title}}'"
        ) {
            sender.execute(SendMessage().apply {
                chatId = chat.id.toString()
                text = error.errorMsg ?: ResponseErrorsType.ERROR.errorMsg!!
                parseMode = "HTML"
            })
        }
    }

    private fun doResponse(
        sender: AbsSender,
        user: User,
        chat: Chat,
        data: AggregatedDataContainer
    ) {
        withErrorLogging(
            logger::error,
            "Error while processing command from user='${user.userName}' to chat=:'{${chat.title}}'"
        ) {
            if (data.cameraSnapshot.image == null || !data.cameraSnapshot.image.exists()) {
                sender.execute(SendMessage().apply {
                    chatId = chat.id.toString()
                    text = makeCaption(data.roadConditions)
                    parseMode = "HTML"
                })
            } else {
                sender.execute(SendPhoto().apply {
                    data.cameraSnapshot.image.let { photo = InputFile(it) }
                    chatId = chat.id.toString()
                    caption = makeCaption(data.roadConditions)
                    parseMode = "HTML"
                })
            }
        }
    }

    fun makeCaption(roadConditions: RoadConditionsContainer?): String = buildString {
        val titleSuffix =
            roadConditions?.updatedAt?.let { " <i>(of ${it.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))})</i>" }
                ?: ""
        append("<b>Cyprus Road Conditions${titleSuffix}</b>")
        append("\n\n")
        roadConditions?.roads?.forEach {
            append("${it.roadStatus?.icon ?: ""} <b>${it.src} - ${it.dst}</b> <i>(${it.roadStatus?.message})</i>").append(
                "\n"
            )
        }
    }

    companion object : KLogging() {
        enum class ResponseErrorsType(val errorMsg: String? = null) {
            NOT_PRESENT_ERR("Cyprus Road Conditions aren't present. It may be a technical error on server side."),
            ERROR("Internal error. I can't proceed your request")
        }
    }

}
