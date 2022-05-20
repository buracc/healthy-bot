package com.github.buracc.healthybot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HealthyBotApplication

fun main(args: Array<String>) {
    runApplication<HealthyBotApplication>(*args)
}
