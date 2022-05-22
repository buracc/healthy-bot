package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.SettingConstants
import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.CasinoException
import com.github.buracc.healthybot.discord.exception.CommandDisabledException
import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.discord.helper.Utils.filterMember
import com.github.buracc.healthybot.discord.model.DiscordCommand
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.random.Random

@Component
class IncomeCommandHandler(
    override val jda: JDA,
    private val settingService: SettingService,
    private val userService: UserService
) : SlashCommandHandler() {
    override val commands = mapOf(
        "rob" to DiscordCommand(
            name = "rob",
            description = "Attempts to rob a user if they have cash.",
            handler = ::rob,
            options = mapOf(
                "user" to OptionData(OptionType.STRING, "user", "The user to rob.", true)
            )
        ),
        "work" to DiscordCommand(
            name = "work",
            description = "Work a job and earn money.",
            handler = ::work
        ),
        "crime" to DiscordCommand(
            name = "crime",
            description = "Commit a crime to earn money.",
            handler = ::crime
        ),
        "slut" to DiscordCommand(
            name = "slut",
            description = "Perform a slutty act to earn money.",
            handler = ::slut
        )
    )

    fun rob(event: SlashCommandInteractionEvent): String {
        if (!settingService.getBoolean(SettingConstants.ROB_SETTING)) {
            throw CommandDisabledException()
        }
        val selectedUser = event.getOption("user")?.asString
            ?: throw NotFoundException("No user specified.")
        val user = userService.findById(event.user.id)
        val cd = (user.robCooldown.toEpochMilli() - Instant.now().toEpochMilli()) / 1000
        if (cd > 0) {
            throw BotException("You cannot rob a user for another $cd seconds.")
        }
        val discordUser =
            event.guild?.members
                ?.firstOrNull(filterMember(selectedUser))
                ?: throw NotFoundException("Couldn't find user: ${selectedUser}.")

        val target = userService.findById(discordUser.id)
        val fine = getCashReward(abs(user.balance))
        user.robCooldown = Instant.now().plusMillis(settingService.getLong(SettingConstants.ROB_CD))

        if (target.cash < 0) {
            userService.save(user.also { it.cash -= getCashReward(settingService.getInt(SettingConstants.INCOME_BASE_RATE)) + fine })
            throw CasinoException("You have been fined $fine for trying to rob a poor person.")
        }

        if (ThreadLocalRandom.current().nextBoolean()) {
            userService.save(user.also { it.cash -= getCashReward(settingService.getInt(SettingConstants.INCOME_BASE_RATE)) + fine })
            throw CasinoException("You failed to rob ${discordUser.user.asMention}, and were fined $fine.")
        }

        userService.save(user.also { it.cash += fine })
        userService.save(target.also { it.cash -= fine })
        return "You stole $fine from ${discordUser.user.asMention}."
    }

    private fun work(event: SlashCommandInteractionEvent): String {
        if (!settingService.getBoolean(SettingConstants.WORK_SETTING)) {
            throw CommandDisabledException()
        }

        val user = userService.findById(event.user.id)
        val cd = (user.workCooldown.toEpochMilli() - Instant.now().toEpochMilli()) / 1000
        if (cd > 0) {
            throw BotException("You cannot work for another $cd seconds.")
        }

        user.workCooldown = Instant.now().plusMillis(settingService.getLong(SettingConstants.WORK_CD))
        val reward = getCashReward(settingService.getInt(SettingConstants.INCOME_BASE_RATE))
        userService.save(user.also { it.cash += reward })
        return "You work and receive $reward."
    }

    private fun crime(event: SlashCommandInteractionEvent): String {
        if (!settingService.getBoolean(SettingConstants.CRIME_SETTING)) {
            throw CommandDisabledException()
        }
        val user = userService.findById(event.user.id)
        val cd = (user.crimeCooldown.toEpochMilli() - Instant.now().toEpochMilli()) / 1000
        if (cd > 0) {
            throw BotException("You cannot commit a crime for another $cd seconds.")
        }

        val reward = getCashReward(settingService.getInt(SettingConstants.INCOME_BASE_RATE))
        user.crimeCooldown = Instant.now().plusMillis(settingService.getLong(SettingConstants.CRIME_CD))
        if (ThreadLocalRandom.current().nextBoolean()) {
            val fine = reward + getCashReward(abs(user.balance))
            userService.save(user.also { it.cash -= fine })
            throw BotException("You failed to commit a crime and paid $fine in damages.")
        }

        userService.save(user.also { it.cash += reward })
        return "You commit a crime and receive $reward."
    }

    private fun slut(event: SlashCommandInteractionEvent): String {
        if (!settingService.getBoolean(SettingConstants.SLUT_SETTING)) {
            throw CommandDisabledException()
        }
        val user = userService.findById(event.user.id)
        val cd = (user.slutCooldown.toEpochMilli() - Instant.now().toEpochMilli()) / 1000
        if (cd > 0) {
            throw BotException("You cannot be a slut for another $cd seconds.")
        }

        val reward = getCashReward(settingService.getInt(SettingConstants.INCOME_BASE_RATE))
        user.slutCooldown = Instant.now().plusMillis(settingService.getLong(SettingConstants.SLUT_CD))
        if (ThreadLocalRandom.current().nextBoolean()) {
            val fine = reward + getCashReward(abs(user.balance))
            userService.save(user.also { it.cash -= fine })
            throw BotException("You failed to be a slut and paid $fine in damages.")
        }

        userService.save(user.also { it.cash += reward })
        return "You're a good slut, so you receive $reward."
    }

    private fun getCashReward(balance: Int): Int {
        return (balance * Random.nextDouble(
            settingService.getDouble(SettingConstants.INCOME_MIN_RATIO),
            settingService.getDouble(SettingConstants.INCOME_MAX_RATIO)
        )).toInt()
    }
}