package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.repository.ReminderRepository
import com.github.buracc.healthybot.repository.entity.Reminder
import com.github.buracc.healthybot.repository.entity.User
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.transaction.Transactional

@Service
class ReminderService(
    private val reminderRepository: ReminderRepository
) {
    companion object {
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm z")
    }

    fun getAll() = reminderRepository.findAll()

    fun getById(id: Long) = reminderRepository.findById(id)
        .orElseThrow { UnauthorizedException("Reminder not found.") }

    fun getAllByOwner(user: User) = reminderRepository.findAllByOwner(user)

    @Transactional
    fun deleteById(id: Long) = reminderRepository.deleteById(id)

    @Transactional
    fun deleteAll(reminders: List<Reminder>) = reminderRepository.deleteAll(reminders)

    fun add(time: String, message: String, user: User): Reminder {
        try {
            val dateTime = ZonedDateTime.parse(time, format)
            if (dateTime.toInstant() < Instant.now()) {
                throw BotException("Reminder needs to be in the future.")
            }

            val reminder = Reminder(
                message = message,
                remindDateString = time,
                owner = user
            )
            return reminderRepository.save(reminder)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            throw BotException("Failed to add reminder, make sure the formatting is correct (eg. 30-09-2022 19:24 +01:00)")
        }
    }
}