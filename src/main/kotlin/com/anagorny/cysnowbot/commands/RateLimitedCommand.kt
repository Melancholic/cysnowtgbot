package com.anagorny.cysnowbot.commands

import com.anagorny.cysnowbot.services.RateLimiter
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.math.max

abstract class RateLimitedCommand(
    commandIdentifier: String,
    description: String,
    private val rateLimiter: RateLimiter
) : Command(commandIdentifier, description) {

    override fun processMessage(sender: AbsSender, message: Message, arguments: Array<out String>) {
        if (rateLimiter.isRequestAllowed(getRLKey(message.chat, message.from))) {
            super.processMessage(sender, message, arguments)
        } else {
            val minutes = max(rateLimiter.howLongForAllow(getRLKey(message.chat, message.from)).toMinutes(), 1)
            val text = "@${message.from.userName}, the limit has been exceeded on requests to the bot for this chat. " +
                    "Try again after $minutes minutes."
            doReply(
                sender,
                message,
                text
            )
        }
    }

    override fun execute(sender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        doExecute(sender, user, chat, arguments)
    }

    private fun rlChecking(chat: Chat, user: User): Boolean {
        if (rateLimiter.isRateLimitingEnabled()) {
        }
        return true
    }

    protected open fun getRLKey(chat: Chat, user: User): String = chat.id.toString()
}
