package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.helper.Utils.parseDateTime
import com.github.buracc.healthybot.repository.ReminderRepository
import com.github.buracc.healthybot.repository.entity.Reminder
import com.github.buracc.healthybot.repository.entity.User
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ReminderService(
    private val reminderRepository: ReminderRepository
) {
    fun getAll() = reminderRepository.findAll().toList()

    fun getById(id: Long) = reminderRepository.findById(id)
        .orElseThrow { UnauthorizedException("Reminder not found.") }

    fun getAllByOwner(user: User) = reminderRepository.findAllByOwner(user)

    @Transactional
    fun deleteById(id: Long) = reminderRepository.deleteById(id)

    @Transactional
    fun deleteAll(reminders: List<Reminder>) = reminderRepository.deleteAll(reminders)

    fun add(time: String, message: String, user: User): Reminder {
        try {
            val dateTime = parseDateTime(time)
            if (dateTime.toInstant() < Instant.now()) {
                throw BotException("Reminder needs to be in the future.")
            }

            val reminder = Reminder(
                message = message,
                remindDateString = time,
                owner = user
            )
            return reminderRepository.save(reminder)
        } catch (e: Exception) {
            throw BotException("Failed to add reminder, make sure the formatting is correct (eg. 30-09-2022 19:24 Europe/Amsterdam). " +
                    "Check https://en.wikipedia.org/wiki/List_of_tz_database_time_zones for a list of supported timezones (under TZ identifier).")
        }
    }
}