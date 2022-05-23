package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.SettingConstants.BANK_SETTING
import com.github.buracc.healthybot.discord.exception.CasinoException
import com.github.buracc.healthybot.discord.exception.CommandDisabledException
import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.discord.helper.Utils
import com.github.buracc.healthybot.discord.model.DiscordCommand
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component

@Component
class BankCommandHandler(
    override val jda: JDA,
    private val userService: UserService,
    private val settingService: SettingService
) : SlashCommandHandler() {
    override val commands = mapOf(
        "balance" to DiscordCommand(
            name = "balance",
            description = "Shows your current balance.",
            handler = ::balance,
            options = mapOf(
                "user" to OptionData(OptionType.STRING, "user", "Check specified user's balance.", false)
            )
        ),
        "withdraw" to DiscordCommand(
            name = "withdraw",
            description = "Withdraw money from your bank account.",
            handler = ::withdraw,
            options = mapOf(
                "amount" to OptionData(OptionType.STRING, "amount", "Amount of cash to withdraw", true)
            )
        ),
        "deposit" to DiscordCommand(
            name = "deposit",
            description = "Deposit money to your bank account.",
            handler = ::deposit,
            options = mapOf(
                "amount" to OptionData(OptionType.STRING, "amount", "Amount of cash to deposit", true)
            )
        )
    )

    private fun balance(event: SlashCommandInteractionEvent): String {
        val selectedUser = event.getOption("user")?.asString
        val userId = if (selectedUser == null) {
            event.user.id
        } else {
            event.guild?.members?.firstOrNull(Utils.filterMember(selectedUser))?.id
                ?: throw NotFoundException("Couldn't find user: ${selectedUser}.")
        }

        val user = userService.findById(userId)
        return "${jda.getUserById(user.discordId)?.asMention}'s balance: ${user.cash} Cash | ${user.bank} Banked"
    }

    private fun withdraw(event: SlashCommandInteractionEvent): String {
        val amount = bank(event)
        val user = userService.findById(event.user.id)
        if (user.bank <= 0) {
            throw CasinoException("You do not have any money in your bank account.")
        }

        val withdrawn = if (amount > user.bank) user.bank else amount
        userService.save(user.also {
            it.cash += withdrawn
            it.bank -= withdrawn
        })

        return "You withdrew $withdrawn from your bank account."
    }

    private fun deposit(event: SlashCommandInteractionEvent): String {
        val amount = bank(event)
        val user = userService.findById(event.user.id)
        if (user.cash <= 0) {
            throw CasinoException("You do not have any cash to deposit.")
        }

        val deposited = if (amount > user.cash) user.cash else amount
        userService.save(user.also {
            it.cash -= deposited
            it.bank += deposited
        })

        return "You deposited $deposited to your bank account."
    }

    private fun bank(event: SlashCommandInteractionEvent): Int {
        if (!settingService.getBoolean(BANK_SETTING)) {
            throw CommandDisabledException()
        }

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

        return amount
    }
}