package com.anagorny.cysnowbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
class CySnowTelegramBot

fun main(args: Array<String>) {
	runApplication<CySnowTelegramBot>(*args)
}

