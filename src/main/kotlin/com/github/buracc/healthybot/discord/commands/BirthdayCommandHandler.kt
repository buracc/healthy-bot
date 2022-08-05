package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

@Component
class BirthdayCommandHandler(
    override val jda: JDA,
    private val userService: UserService
) : CommandHandler() {
    private val datePattern = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}$"
    )

    override fun handle(command: Command, message: Message) {
        respond({
            when (command.actions.getOrNull(0)) {
                "add" -> add(command)
                else -> display()
            }
        }, message)
    }

    private fun display(): EmbedBuilder {
        val embed = embedHelper.builder("Birthdays this month")
        val now = LocalDate.now()
        embed.setDescription("To register your birth day, do !bday year-month-day")
        for (user in userService.findAll()) {
            val date = user.birthDate ?: continue
            if (now.monthValue != date.monthValue) {
                continue
            }

            embed.addField("<@${user.discordId}>", date.toString(), false)
        }

        return embed
    }

    private fun add(command: Command): String {
        val date = command.actions[1]
        if (!datePattern.matcher(date).matches()) {
            throw BotException("Invalid date entered. Correct formatting: year-month-day (ex. 1990-08-17).")
        }

        try {
            LocalDate.parse(date)
            val user = userService.findByIdOrCreate(command.userId.toString())
            userService.save(user.also { it.birthday = date })
            return "Successfully registered your birthday: $date"
        } catch (e: DateTimeParseException) {
            throw BotException("Invalid date entered. Correct formatting: year-month-day (ex. 1990-08-17).")
        }
    }
}