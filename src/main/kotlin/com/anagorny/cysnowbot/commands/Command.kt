package com.anagorny.cysnowbot.commands

import com.anagorny.cysnowbot.helpers.withErrorLogging
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient

abstract class Command(
    commandIdentifier: String,
    description: String,
) : BotCommand(commandIdentifier, description) {
    abstract fun doExecute(sender: TelegramClient, user: User, chat: Chat, arguments: Array<out String>)

    protected fun doReply(sender: TelegramClient, message: Message, response: String): Message? {
        return withErrorLogging(
            { msg, e -> StatusCommand.logger.error(e) { msg } },
            "Error while replying to message=${message.messageId} from user='${message.from.userName}' in the chat='{${message.chat.title}}'"
        ) {
            sender.execute(
                SendMessage.builder()
                    .replyToMessageId(message.messageId)
                    .chatId(message.chatId.toString())
                    .text(response)
                    .parseMode("HTML")
                    .build()
            )
        }
    }

    protected fun sentAction(sender: TelegramClient, chat: Chat, action: ActionType) {
        sender.execute(
            SendChatAction.builder()
                .chatId(chat.id)
                .action(action.toString())
                .build()
        )
    }
}

