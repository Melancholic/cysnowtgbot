package com.anagorny.cysnowbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
//@EnableCaching
@EnableScheduling
@EnableAsync
class CySnowTelegramBot

fun main(args: Array<String>) {
	runApplication<CySnowTelegramBot>(*args)
}

