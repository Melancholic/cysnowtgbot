package com.anagorny.cysnowbot

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Guards the consume(List) workaround in MainTelegramBotService (see comment there).
class IsCommandReproTest {

    private fun commandMessage() = Message.builder()
        .messageId(1)
        .text("/status")
        // Real Telegram JSON has no MessageEntity.text - it's backfilled by getEntities().
        .entities(listOf(MessageEntity.builder().type("bot_command").offset(0).length(7).build()))
        .chat(Chat.builder().id(1L).type("private").build())
        .from(User.builder().id(1L).firstName("test").isBot(false).build())
        .build()

    @Test
    fun `isCommand is false until getEntities has been called once`() {
        val message = commandMessage()
        assertFalse(message.isCommand, "the underlying library bug this workaround exists for")

        message.entities

        assertTrue(message.isCommand, "touching entities is what makes isCommand work")
    }

    @Test
    fun `a command reaches execute instead of falling through to processNonCommandUpdate`() {
        // consume(List) dispatches to an executor, so wait for whichever path runs.
        val dispatched = CountDownLatch(1)
        var executed = false
        var fellThrough = false

        val command = object : BotCommand("status", "test command") {
            override fun execute(client: TelegramClient, user: User, chat: Chat, args: Array<out String>) {
                executed = true
                dispatched.countDown()
            }
        }

        // Mirrors MainTelegramBotService: same base class, same consume(List) override.
        val bot = object : CommandLongPollingTelegramBot(mock(TelegramClient::class.java), true, { "test_bot" }) {
            override fun consume(updates: List<Update>) {
                updates.forEach { it.message?.entities }
                super.consume(updates)
            }

            override fun processNonCommandUpdate(update: Update) {
                fellThrough = true
                dispatched.countDown()
            }
        }
        bot.register(command)

        val update = Update().apply {
            updateId = 1
            message = commandMessage()
        }
        bot.consume(listOf(update))

        assertTrue(dispatched.await(5, TimeUnit.SECONDS), "update was never dispatched")
        assertTrue(executed, "command should have been dispatched to execute()")
        assertFalse(fellThrough, "command should not fall through to processNonCommandUpdate")
    }
}
