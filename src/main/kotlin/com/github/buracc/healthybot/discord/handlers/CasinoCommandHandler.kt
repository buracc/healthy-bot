package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.discord.helper.Utils.filterMember
import com.github.buracc.healthybot.discord.model.DiscordCommand
import com.github.buracc.healthybot.repository.entity.Role
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
    private val embedHelper: EmbedHelper
) : SlashCommandHandler() {
    override val commands = mapOf(
        "reset" to DiscordCommand(
            name = "reset",
            description = "Resets the casino.",
            handler = ::reset
        ),
        "balance" to DiscordCommand(
            name = "balance",
            description = "Shows your current balance.",
            handler = ::balance,
            options = mapOf(
                "user" to OptionData(OptionType.STRING, "user", "Check specified user's balance.", false)
            )
        ),
        "top10" to DiscordCommand(
            name = "top10",
            description = "Shows the leaderboard.",
            handler = ::top10
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

    private fun balance(event: SlashCommandInteractionEvent): String {
        val selectedUser = event.getOption("user")?.asString
        val userId = if (selectedUser == null) {
            event.user.id
        } else {
            event.guild?.members?.firstOrNull(filterMember(selectedUser))?.id
                ?: throw NotFoundException("Couldn't find user: ${selectedUser}.")
        }

        val user = userService.findById(userId)
        return "${jda.getUserById(user.discordId)?.asMention}'s balance: ${user.cash} Cash | ${user.bank} Banked"
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
}