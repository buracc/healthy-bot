package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.SettingConstants.ROULETTE_SETTING
import com.github.buracc.healthybot.discord.exception.CasinoException
import com.github.buracc.healthybot.discord.exception.CommandDisabledException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.discord.managers.roulette.RouletteManager
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
class CasinoCommandHandler(
    override val jda: JDA,
    private val userService: UserService,
    private val embedHelper: EmbedHelper,
    private val settingService: SettingService,
    private val rouletteManager: RouletteManager
) : SlashCommandHandler() {
    override val commands = mapOf(
        "reset" to DiscordCommand(
            name = "reset",
            description = "Resets the casino.",
            handler = ::reset
        ),
        "top10" to DiscordCommand(
            name = "top10",
            description = "Shows the leaderboard.",
            handler = ::top10
        ),
        "roulette" to DiscordCommand(
            name = "roulette",
            description = "Place a roulette bet.",
            handler = ::roulette,
            options = mapOf(
                "amount" to OptionData(OptionType.STRING, "amount", "The amount to bet.", true),
                "space" to OptionData(OptionType.STRING, "space", "The space to bet on.", true)
            )
        ),
    )

    private fun reset(event: SlashCommandInteractionEvent): String {
        val user = userService.findById(event.user.id)
        if (user.role != Role.ADMIN) {
            throw UnauthorizedException("You are not authorized to run this command.")
        }

        userService.clear()

        return "Successfully reset the casino."
    }

    private fun top10(event: SlashCommandInteractionEvent): EmbedBuilder {
        val builder = embedHelper.builder("Casino Top 10")

        userService.findTop10().forEachIndexed { i, user ->
            builder.addField(
                "${i + 1}. ${jda.getUserById(user.discordId)?.asTag}",
                "Total balance: ${user.balance}",
                false
            )
        }

        return builder
    }

    private fun roulette(event: SlashCommandInteractionEvent): String {
        if (!settingService.getBoolean(ROULETTE_SETTING)) {
            throw CommandDisabledException()
        }
        val space = event.getOption("space")?.asString ?: throw CasinoException("Invalid space entered.")
        val amount = event.getOption("amount")?.asString
            .run {
                if (this == "all") {
                    Int.MAX_VALUE
                } else {
                    this?.toInt()
                }
            }
            ?: throw CasinoException("Invalid amount entered.")
        if (amount < 0) {
            throw CasinoException("Invalid amount entered.")
        }

        rouletteManager.bet(space, event.user.id, amount, event)
        return "Placed a bet of $amount on $space."
    }
}