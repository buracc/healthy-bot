package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.CasinoException
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
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

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
        "rob" to DiscordCommand(
            name = "rob",
            description = "Attempts to rob a user if they have cash.",
            handler = ::rob,
            options = mapOf(
                "user" to OptionData(OptionType.STRING, "user", "The user to rob.", true)
            )
        )
    )

    private fun setup(event: SlashCommandInteractionEvent): String {
        val user = userService.findById(event.user.id)
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
            event.guild?.members?.firstOrNull {
                it.nickname?.lowercase()?.contains(selectedUser) == true
                        || it.effectiveName.lowercase().contains(selectedUser)
            }?.id ?: throw NotFoundException("Couldn't find user: ${selectedUser}.")
        }

        val user = userService.findById(userId)
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

    private fun rob(event: SlashCommandInteractionEvent): String {
        val selectedUser = event.getOption("user")?.asString
            ?: throw NotFoundException("No user specified.")

        val user = userService.findById(event.user.id)
        if (user.robCooldown > 0) {
            throw BotException("You cannot rob a user for another ${user.robCooldown / 1000} seconds.")
        }
        val discordUser = event.guild?.members?.firstOrNull {
            it.nickname?.lowercase()?.contains(selectedUser) == true
                    || it.effectiveName.lowercase().contains(selectedUser)
        } ?: throw NotFoundException("Couldn't find user: ${selectedUser}.")

        val target = userService.findById(discordUser.id)
        val fine = (user.balance * Random.nextDouble(0.0, 0.8)).toInt()
        user.lastRob = Instant.now().plusMillis(60_000)

        if (target.cash < 0) {
            userService.save(user.also { it.cash -= fine })
            throw CasinoException("You have been fined $fine for trying to rob a poor person.")
        }

        if (ThreadLocalRandom.current().nextBoolean()) {
            userService.save(user.also { it.cash -= fine })
            throw CasinoException("You failed to rob ${discordUser.user.asMention}, and was fined $fine.")
        }

        userService.save(user.also { it.cash += fine })
        userService.save(target.also { it.cash -= fine })
        return "You stole $fine from ${discordUser.user.asMention}."
    }
}