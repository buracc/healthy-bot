package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.repository.ReminderRepository
import com.github.buracc.healthybot.repository.entity.Reminder
import com.github.buracc.healthybot.repository.entity.User
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.transaction.Transactional

@Service
class ReminderService(
    private val reminderRepository: ReminderRepository
) {
    private val format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm z")

    fun getAll() = reminderRepository.findAll()

    @Transactional
    fun delete(reminders: List<Reminder>) = reminderRepository.deleteAll(reminders)

    @Transactional
    fun add(time: String, message: String, user: User): Reminder {
        try {
            val dateTime = ZonedDateTime.parse(time, format)
            if (dateTime.toInstant() < Instant.now()) {
                throw BotException("Reminder needs to be in the future.")
            }

            val reminder = Reminder(
                message = message,
                remindDate = dateTime,
                owner = user
            )
            return reminderRepository.save(reminder)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            throw BotException("Failed to add reminder, make sure the formatting is correct (eg. 30-09-2022 19:24 +01:00)")
        }
    }
}