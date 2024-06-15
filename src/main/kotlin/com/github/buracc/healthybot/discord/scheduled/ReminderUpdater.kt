package com.github.buracc.healthybot.discord.scheduled

import com.github.buracc.healthybot.discord.helper.Utils.TZ
import com.github.buracc.healthybot.discord.helper.Utils.now
import com.github.buracc.healthybot.service.ReminderService
import net.dv8tion.jda.api.entities.Guild
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReminderUpdater(
    private val guild: Guild,
    private val reminderService: ReminderService
) {
    companion object {
        private const val category = "1251168329532702820"
    }

    init {
        checkAndAnnounce()
    }

    @Scheduled(cron = "0 */10 * * * *")
    fun checkAndAnnounce() {
        val remindersToday = reminderService.getAll()
            .map {
                it.copy(
                    remindDateString = it.remindDate.withZoneSameInstant(TZ)
                        .format(ReminderService.format)
                )
            }
            .filter { it.remindDate.toLocalDate() == now().toLocalDate() }
            .sortedBy { it.remindDate }
            .take(3)

        val category = guild.getCategoryById(category) ?: return

        category.voiceChannels.forEach { it.delete().queue() }

        for (reminder in remindersToday) {
            val eventTime = reminder.remindDate.toLocalTime().toString()

            guild.createVoiceChannel("$eventTime - ${reminder.message}")
                .setParent(category)
                .queue()
        }
    }
}