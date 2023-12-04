package com.github.buracc.healthybot.discord.listeners

import com.github.buracc.healthybot.discord.SettingConstants.COMMAND_PREFIX
import com.github.buracc.healthybot.discord.commands.*
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.repository.MarkovRepository
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class ChatListener(
    private val jda: JDA,
    private val guild: Guild,
    private val userService: UserService,
    private val settingService: SettingService,
    private val birthdayCommandHandler: BirthdayCommandHandler,
    private val settingsCommandHandler: SettingsCommandHandler,
    private val userCommandHandler: UserCommandHandler,
    private val reminderCommandHandler: ReminderCommandHandler,
    private val markovCommandHandler: MarkovCommandHandler,
    private val markovRepository: MarkovRepository,
    private val aiCommandHandler: AICommandHandler
) : ListenerAdapter() {
    val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun register() {
        jda.addEventListener(this)

        val guildCommands = guild.retrieveCommands().complete()
        guildCommands.forEach {
            logger.info("Deleting command '${it.name}'")
            it.delete().complete()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val member = event.member ?: return
        if (member.id == jda.selfUser.id) {
            return
        }

        val user = userService.findByIdOrCreate(member.id)
        user.lastMessage = event.message.timeCreated.toInstant()
        userService.save(user)

        val memberId = member.idLong
        val message = event.message
        val messageContent = message.contentRaw

        storeMarkovMessage(member.id, messageContent)

        val split = messageContent.split(" ", "\n")
        if (split.isEmpty()) {
            return
        }

        val commandText = split.getOrNull(0) ?: return
        val commandPrefix = commandText.getOrNull(0) ?: return
        val prefixSetting = settingService.get(COMMAND_PREFIX).getOrNull(0) ?: return
        if (commandPrefix != prefixSetting) {
            return
        }

        val actions = if (split.size == 1) emptyArray() else split.subList(1, split.size).toTypedArray()
        val trimmedContent = messageContent.replace(commandText, "").trim()
        val command = Command(
            memberId,
            commandText.substring(1),
            trimmedContent,
            actions,
            message.guildChannel.id
        )

        when (command.command) {
            "bday" -> birthdayCommandHandler.handle(command, message)
            "settings" -> settingsCommandHandler.handle(command, message)
            "user", "users", "inthards" -> userCommandHandler.handle(command, message)
            "remind", "reminder", "reminders" -> reminderCommandHandler.handle(command, message)
            "markov" -> markovCommandHandler.handle(command, message)
            "ai" -> aiCommandHandler.handle(command, message)
        }
    }

    private fun storeMarkovMessage(userId: String, message: String) {
        markovRepository.store(userId, message)
    }
}