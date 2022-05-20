package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.model.DiscordCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
        event.reply(command.reply).setEphemeral(command.private).queue()
        command.handler(event)
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
    protected abstract val commands: Map<String, DiscordCommand>
}