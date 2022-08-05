package com.github.buracc.healthybot.discord.listeners

import com.github.buracc.healthybot.config.properties.DiscordProperties
import com.github.buracc.healthybot.discord.SettingConstants.COMMAND_PREFIX
import com.github.buracc.healthybot.discord.commands.BirthdayCommandHandler
import com.github.buracc.healthybot.discord.commands.SettingsCommandHandler
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.service.SettingService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ChatCommandListener(
    private val jda: JDA,
    private val settingService: SettingService,
    private val discordProperties: DiscordProperties,
    private val birthdayCommandHandler: BirthdayCommandHandler,
    private val settingsCommandHandler: SettingsCommandHandler
) : ListenerAdapter() {
    val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun register() {
        jda.addEventListener(this)

        val guild = jda.getGuildById(discordProperties.guildId) ?: return
        val guildCommands = guild.retrieveCommands().complete()
        guildCommands.forEach {
            logger.info("Deleting command '${it.name}'")
            it.delete().complete()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val member = event.member?.idLong ?: return
        val message = event.message
        val messageContent = message.contentStripped
        val split = messageContent.split(" ")
        if (split.isEmpty()) {
            logger.warn("Message was empty")
            return
        }

        val first = split[0]
        val prefix = first[0]
        val prefixSetting = settingService.get(COMMAND_PREFIX)[0]
        if (prefix != prefixSetting) {
            return
        }

        val command = Command(
            member,
            first.substring(1),
            split.subList(1, split.size).toTypedArray()
        )

        when (command.command) {
            "bday" -> birthdayCommandHandler.handle(command, message)
            "settings" -> settingsCommandHandler.handle(command, message)
        }
    }
}