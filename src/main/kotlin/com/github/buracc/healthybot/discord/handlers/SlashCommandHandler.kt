package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.CasinoException
import com.github.buracc.healthybot.discord.model.DiscordCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.awt.Color
import javax.annotation.PostConstruct

abstract class SlashCommandHandler : ListenerAdapter() {
    @Value("\${discord.bot.guild-id}")
    private lateinit var guildId: String
    private val logger = LoggerFactory.getLogger(SlashCommandHandler::class.java)

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commandName = event.name
        val command = commands[commandName] ?: return

        val sender = event.user
        logger.info("Command '${event.name}' received from ${sender.name}, ${event.options}")
        event.deferReply().setEphemeral(command.private).queue()

        try {
            when (val response = command.handler(event)) {
                is String -> {
                    event.hook
                        .sendMessageEmbeds(
                            EmbedBuilder()
                                .setTitle("Casino")
                                .setFooter("HealthyBot")
                                .setColor(Color.YELLOW)
                                .setThumbnail(jda.selfUser.avatarUrl)
                                .setDescription(response)
                                .build()
                        )
                        .queue()
                }

                is MessageEmbed -> {
                    event.hook
                        .sendMessageEmbeds(response)
                        .queue()
                }
            }

        } catch (ex: BotException) {
            val embed = EmbedBuilder()
                .setFooter("HealthyBot")
                .setColor(Color.RED)
                .setThumbnail(jda.selfUser.avatarUrl)
                .setDescription(ex.message)

            if (ex is CasinoException) {
                embed.setTitle("Casino")
            }

            event.hook
                .sendMessageEmbeds(embed.build())
                .queue()
        }
    }

    @PostConstruct
    fun register() {
        jda.addEventListener(this)
        val commandData = mutableListOf<CommandData>()
        commands.forEach { (_, command) ->
            commandData.add(command.buildSlashCommand())
        }

        val guild = jda.getGuildById(guildId)
        if (guild == null) {
            logger.error("Could not find guild with id $guildId")
            return
        }

        // cache members idk
        guild.loadMembers().get()

//        val guildCommands = guild.retrieveCommands().complete()
//        guildCommands.forEach {
//            logger.info("Deleting command '${it.name}'")
//            it.delete().complete()
//        }

        commandData.forEach {
            logger.info("Registering command '${it.name}'")
            guild.upsertCommand(it).complete()
        }
    }

    protected abstract val jda: JDA
    protected abstract val commands: Map<String, DiscordCommand<*>>
}