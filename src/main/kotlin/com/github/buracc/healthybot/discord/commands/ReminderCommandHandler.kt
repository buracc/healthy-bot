package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.repository.entity.Role
import com.github.buracc.healthybot.service.ReminderService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class ReminderCommandHandler(
    override val jda: JDA,
    private val userService: UserService,
    private val reminderService: ReminderService
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
        respond({
            when (command.actions.getOrNull(0)) {
                "remove" -> remove(command)
                else -> add(command)
            }
        }, message)
    }

    fun remove(command: Command): String {
        val id = command.actions.getOrNull(1)?.toLongOrNull() ?: throw BotException("No id provided")
        val reminder = reminderService.getById(id)
        val user = userService.findByIdOrCreate(command.userId)
        if (reminder.owner != user) {
            throw UnauthorizedException("You do not own this reminder.")
        }

        reminderService.deleteById(reminder.id!!)

        return "Successfully removed reminder #${reminder.id}."
    }

    fun add(command: Command): Any {
        val user = userService.findByIdOrCreate(command.userId)
        if (user.role == Role.ADMIN || user.authorized) {
            val messages = command.messageTrimmed.split(";", "\n").filter { it.isNotBlank() }
            if (messages.isEmpty()) {
                val embed = embedHelper.builder("Reminders")
                val reminders = if (command.command == "reminders") reminderService.getAll() else
                    reminderService.getAllByOwner(user)
                val now = ZonedDateTime.now()
                embed.setFooter(
                    "Reminder example: !remind ${now.format(ReminderService.format)} f1 time turds"
                )

                if (reminders.isEmpty()) {
                    embed.setDescription("No reminders found.")
                    return embed
                }

                
                for (reminder in reminders.sortedBy { it.remindDate }) {
                    val parsed = ZonedDateTime.parse(reminder.remindDateString, ReminderService.format)
                    embed.addField(
                        "#${reminder.id}. ${reminder.message.trim()}",
                        "<t:${parsed.toInstant().getEpochSecond()}>",
                        false
                    )
                }

                return embed
            }

            var created = 0
            for (msg in messages) {
                val actions = msg.split(" ")
                val datetime = "${actions[0]} ${actions[1]} ${actions[2]}"
                val message = actions.toList().subList(3, actions.size).joinToString(" ")
                reminderService.add(datetime, message, user).let { created++ }
            }

            return "Created $created reminders."
        }

        throw UnauthorizedException("You are not authorized to use this command.")
    }
}
