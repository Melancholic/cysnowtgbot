package com.anagorny.cysnowbot.commands

import mu.KLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class StopCommand : BotCommand("stop", "Stop using this bot") {

    override fun execute(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        val answer = SendMessage()
        answer.chatId = chat.id.toString()
        answer.text = "Good bye ${user.firstName}!\nHope to see you soon!"
        try {
            absSender.execute(answer)
        } catch (e: TelegramApiException) {
            logger.error("Error while processing command from user='{}': ", user.userName, e)
        }
    }

    companion object : KLogging()
}
