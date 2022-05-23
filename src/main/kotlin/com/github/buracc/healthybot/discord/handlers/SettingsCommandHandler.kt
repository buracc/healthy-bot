package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.discord.model.DiscordCommand
import com.github.buracc.healthybot.repository.entity.Role
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component

@Component
class SettingsCommandHandler(
    override val jda: JDA,
    private val userService: UserService,
    private val settingService: SettingService,
    private val embedHelper: EmbedHelper
) : SlashCommandHandler() {
    override val commands = mapOf(
        "settings" to DiscordCommand(
            name = "settings",
            description = "Displays the current settings.",
            handler = ::settings,
            private = true
        ),
        "set" to DiscordCommand(
            name = "set",
            description = "Used to change settings.",
            handler = ::set,
            private = true,
            options = mapOf(
                "key" to OptionData(OptionType.STRING, "key", "The setting to change.", true),
                "value" to OptionData(OptionType.STRING, "value", "The value to set to.", true),
            )
        )
    )

    fun settings(event: SlashCommandInteractionEvent): EmbedBuilder {
        val embed = embedHelper.builder("HealthyBot Settings")
        settingService.findAll().forEach {
            embed.addField(it.key, it.value, false)
        }
        return embed
    }

    fun set(event: SlashCommandInteractionEvent): String {
        val user = userService.findById(event.user.id)
        if (user.role != Role.ADMIN) {
            throw UnauthorizedException("You are not authorized to change settings.")
        }
        val option = event.getOption("key")?.asString ?: throw NotFoundException("No key supplied.")
        val value = event.getOption("value")?.asString ?: throw NotFoundException("No value supplied.")
        val setting = settingService.findById(option)
        val save = settingService.save(setting.also { it.value = value })
        return "Setting ${save.key} set to ${save.value}"
    }
}