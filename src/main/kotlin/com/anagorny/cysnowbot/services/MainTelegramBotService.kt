package com.anagorny.cysnowbot.services

import com.anagorny.cysnowbot.config.TelegramProperties
import com.anagorny.cysnowbot.handlers.MainHandler
import com.anagorny.cysnowbot.helpers.launchAsync
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import mu.KLogging
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand
import org.telegram.telegrambots.meta.api.objects.Update


@Service
class MainTelegramBotService(
    private val telegramProperties: TelegramProperties,
    commands: Set<IBotCommand>,
    @Qualifier("mainFlowCoroutineScope")
    private val scope: CoroutineScope
) : TelegramLongPollingCommandBot(
    DefaultBotOptions(),
    true,
    telegramProperties.bot.token
) {

    @set:Autowired
    @set:Lazy
    lateinit var mainHandler: MainHandler

    init {
        registerAll(*commands.toTypedArray())
        logger.info { "TelegramBot `${telegramProperties.bot.name}` successfully initialized." }
    }

    @PostConstruct
    protected fun postConstruct() {
        logger.info("${this.javaClass.canonicalName} was initialized")
    }

    override fun getBotUsername() = telegramProperties.bot.name

    override fun processNonCommandUpdate(update: Update) {
        scope.launchAsync {
            MDC.put("correlationId", "${update.message.chatId}-${update.message.messageId}")
            mainHandler.handle(update)
        }.invokeOnCompletion { MDC.clear() }
    }

    companion object : KLogging()
}
