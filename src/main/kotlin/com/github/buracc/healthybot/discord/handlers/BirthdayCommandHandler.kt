package com.github.buracc.healthybot.discord.handlers

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.model.DiscordCommand
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

@Component
class BirthdayCommandHandler(
    override val jda: JDA,
    private val userService: UserService
) : SlashCommandHandler() {
    private val datePattern = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}$"
    )

    override val commands = mapOf(
        "bday-register" to DiscordCommand(
            name = "bday-register",
            description = "Registers your birthday.",
            handler = ::register,
            options = mapOf(
                "date" to OptionData(OptionType.STRING, "date", "Formatting: year-month-day (ex. 1990-08-17).", true),
            )
        )
    )

    fun register(event: SlashCommandInteractionEvent): String {
        val date = event.getOption("date")?.asString ?: throw BotException("Invalid date entered.")
        if (!datePattern.matcher(date).matches()) {
            throw BotException("Invalid date entered. Correct formatting: year-month-day (ex. 1990-08-17).")
        }

        try {
            LocalDate.parse(date)
            val user = userService.findById(event.user.id)
            userService.save(user.also { it.birthday = date })
            return "Successfully registered your birthday: $date"
        } catch (e: DateTimeParseException) {
            throw BotException("Invalid date entered. Correct formatting: year-month-day (ex. 1990-08-17).")
        }
    }
}