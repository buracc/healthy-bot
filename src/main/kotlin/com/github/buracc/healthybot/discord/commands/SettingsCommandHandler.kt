package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.repository.entity.Role
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class SettingsCommandHandler(
    override val jda: JDA,
    private val settingService: SettingService,
    private val userService: UserService
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
        respond({
            when (command.actions.getOrNull(0)) {
                "set" -> set(command)
                else -> display()
            }
        }, message)
    }

    fun display(): EmbedBuilder {
        val embed = embedHelper.builder("HealthyBot Settings")
        settingService.findAll().forEach {
            embed.addField(it.key, it.value, false)
        }

        return embed
    }

    fun set(command: Command): String {
        val user = userService.findByIdOrCreate(command.userId)
        if (user.role != Role.ADMIN) {
            throw UnauthorizedException("You are not authorized to change settings.")
        }
        val option = command.actions.getOrNull(1) ?: throw NotFoundException("No key supplied.")
        val value = command.actions.getOrNull(2) ?: throw NotFoundException("No value supplied.")
        val setting = settingService.findById(option)
        val save = settingService.save(setting.also { it.value = value })
        return "Setting ${save.key} set to ${save.value}"
    }
}