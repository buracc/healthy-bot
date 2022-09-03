package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.repository.entity.Role
import com.github.buracc.healthybot.service.ReminderService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class ReminderCommandHandler(
    override val jda: JDA,
    private val userService: UserService,
    private val reminderService: ReminderService
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
        respond({ add(command) }, message)
    }

    fun add(command: Command): String {
        val user = userService.findByIdOrCreate(command.userId)
        if (user.role != Role.ADMIN) {
            throw UnauthorizedException("You are not authorized to add reminders.")
        }

        val datetime = "${command.actions[0]} ${command.actions[1]} ${command.actions[2]}"
        val message = command.actions.toList().subList(3, command.actions.size).joinToString(" ")
        val reminder = reminderService.add(datetime, message, user)
        return "I will remind you at ${reminder.remindDate}."
    }
}