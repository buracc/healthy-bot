package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.model.DiscordCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class CasinoCommandHandler(
    override val jda: JDA) : SlashCommandHandler() {
    override val commands: Map<String, DiscordCommand> = mapOf(
        "setup" to DiscordCommand(
            name = "setup",
            description = "Sets up a new casino round. Resets the current round.",
            reply = "Setting up...",
            handler = ::setup
        )
    )

    private fun setup(event: SlashCommandInteractionEvent) {

    }
}