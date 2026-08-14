package com.anagorny.cysnowbot.services

import com.anagorny.cysnowbot.config.TelegramProperties
import com.anagorny.cysnowbot.handlers.MainHandler
import com.anagorny.cysnowbot.helpers.launchAsync
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.TelegramClient


@Service
class MainTelegramBotService(
    private val telegramProperties: TelegramProperties,
    commands: Set<IBotCommand>,
    telegramClient: TelegramClient,
    @Qualifier("mainFlowCoroutineScope")
    private val scope: CoroutineScope
) : CommandLongPollingTelegramBot(
    telegramClient,
    true,
    { telegramProperties.bot.name }
), SpringLongPollingBot {

    @set:Autowired
    @set:Lazy
    lateinit var mainHandler: MainHandler

    init {
        registerAll(*commands.toTypedArray())
        logger.info { "TelegramBot `${telegramProperties.bot.name}` successfully initialized." }
    }

    @PostConstruct
    protected fun postConstruct() {
        logger.info { "${this.javaClass.canonicalName} was initialized" }
    }

    // SpringLongPollingBot: what the springboot-longpolling-starter registers with Telegram.
    override fun getBotToken(): String = telegramProperties.bot.token

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = this

    override fun processNonCommandUpdate(update: Update) {
        scope.launchAsync {
            val job = launch {
                MDC.put("correlationId", "${update.message.chatId}-${update.message.messageId}")
                mainHandler.handle(update)
            }
            job.invokeOnCompletion { MDC.clear() }
            job.join()
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
