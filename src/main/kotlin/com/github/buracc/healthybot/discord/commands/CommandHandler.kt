package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.config.properties.DiscordProperties
import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.discord.model.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.awt.Color

abstract class CommandHandler {
    private val logger = LoggerFactory.getLogger(CommandHandler::class.java)

    @Autowired
    protected lateinit var embedHelper: EmbedHelper

    @Autowired
    protected lateinit var discordProperties: DiscordProperties

    fun <T> respond(responseHandler: () -> T, message: Message) {
        var embed = embedHelper.builder()

        try {
            when (val response = responseHandler.invoke()) {
                is String -> {
                    embed
                        .setTitle("Healthy Bot")
                        .setDescription(response)
                }

                is EmbedBuilder -> {
                    embed = response
                }
            }
        } catch (ex: BotException) {
            embed.setTitle("Healthy Bot")
            embed.setColor(Color.RED)
            embed.setDescription(ex.message)
        } catch (ex: Exception) {
            logger.error("Command execution failed.", ex)
            embed.setTitle("Error")
            embed.setColor(Color.RED)
            embed.setDescription("Failed to execute command. Check the logs.")
        } finally {
            val msg = embed.build()
            message
                .replyEmbeds(msg)
                .queue()
        }
    }

    abstract fun handle(command: Command, message: Message)

    protected abstract val jda: JDA
}