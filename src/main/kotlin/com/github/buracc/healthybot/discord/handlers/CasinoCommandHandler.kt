package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.model.DiscordCommand
import com.github.buracc.healthybot.repository.entity.Role
import com.github.buracc.healthybot.repository.entity.User
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Service
import java.awt.Color

@Service
class CasinoCommandHandler(
    override val jda: JDA,
    private val userService: UserService
) : SlashCommandHandler() {
    override val commands: Map<String, DiscordCommand<*>> = mapOf(
        "setup" to DiscordCommand(
            name = "setup",
            description = "Sets up a new casino round. Resets the current round.",
            handler = ::setup
        ),
        "balance" to DiscordCommand(
            name = "balance",
            description = "Shows your current balance,",
            handler = ::balance,
            options = mapOf(
                "user" to OptionData(OptionType.STRING, "user", "Check specified user's balance.", false)
            )
        ),
        "top10" to DiscordCommand(
            name = "top10",
            description = "Shows the leaderboard.",
            handler = ::top10
        )
    )

    private fun setup(event: SlashCommandInteractionEvent): String {
        val user = userService.findById(event.user.id)
            .orElseThrow { UnauthorizedException("User not found.") }
        if (user.role != Role.ADMIN) {
            throw UnauthorizedException("You are not authorized to run this command.")
        }

        val members =
            event.guild?.members?.filter { !it.user.isBot } ?: throw NotFoundException("Failed to load guild members.")
        val users = userService.saveAll(members.map {
            User(
                discordId = it.id,
                cash = 1000
            )
        })

        return "New casino round started with ${users.count()} members."
    }

    private fun balance(event: SlashCommandInteractionEvent): String {
        val selectedUser = event.getOption("user")?.asString
        val userId: String = if (selectedUser == null) {
            event.user.id
        } else {
            event.guild?.members?.also { println(it) }?.firstOrNull {
                it.nickname?.lowercase()?.contains(selectedUser) == true
                        || it.effectiveName.lowercase().contains(selectedUser)
            }?.id ?: throw NotFoundException("Couldn't find user: ${selectedUser}.")
        }

        val user = userService.findById(userId)
            .orElseThrow { UnauthorizedException("User not found.") }

        return "${jda.getUserById(user.discordId)?.asMention}'s balance: ${user.cash} Cash | ${user.bank} Banked"
    }

    private fun top10(event: SlashCommandInteractionEvent): MessageEmbed {
        val builder = EmbedBuilder()
        builder.setTitle("Casino Top 10")
        builder.setColor(Color.YELLOW)
        builder.setFooter("HealthyBot")

        userService.findTop10().forEachIndexed { i, user ->
            builder.addField(
                "${i + 1}. ${jda.getUserById(user.discordId)?.asTag}",
                "Total balance: ${user.balance}",
                false
            )
        }

        return builder.build()
    }
}