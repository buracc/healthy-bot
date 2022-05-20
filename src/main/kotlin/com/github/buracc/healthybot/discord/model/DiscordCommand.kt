package com.github.buracc.healthybot.discord.model

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.data.DataObject

data class DiscordCommand(
    private var name: String,
    val description: String,
    val private: Boolean = false,
    var enabled: Boolean = true,
    val options: Map<String, OptionData> = mapOf(),
    val reply: String = "Processing command...",
    val handler: (SlashCommandInteractionEvent) -> Unit
) : CommandData {
    fun buildSlashCommand(): CommandData {
        return Commands.slash(name, description)
            .addOptions(options.values)
    }

    override fun toData(): DataObject {
        return DataObject.empty()
    }

    override fun setName(name: String): CommandData {
        this.name = name
        return this
    }

    override fun setDefaultEnabled(enabled: Boolean): CommandData {
        this.enabled = enabled
        return this
    }

    override fun getName() = name

    override fun isDefaultEnabled() = enabled

    override fun getType(): Command.Type {
        return Command.Type.SLASH
    }
}