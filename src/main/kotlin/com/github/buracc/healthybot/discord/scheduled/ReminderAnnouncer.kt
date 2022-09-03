package com.github.buracc.healthybot.discord.scheduled

import com.github.buracc.healthybot.discord.SettingConstants
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.service.ReminderService
import com.github.buracc.healthybot.service.SettingService
import net.dv8tion.jda.api.JDA
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ReminderAnnouncer(
    private val jda: JDA,
    private val reminderService: ReminderService,
    private val embedHelper: EmbedHelper,
    private val settingService: SettingService
) {
    @Scheduled(cron = "0 * * * * *")
    fun checkAndAnnounce() {
        val now = Instant.now()
        val reminders = reminderService.getAll()
            .filter { now >= it.remindDate.toInstant() }

        if (reminders.isNotEmpty()) {
            val embed = embedHelper.builder("Reminders")

            reminders.forEach {
                embed.addField(it.message.trim(), "by: <@${it.owner.discordId}>", false)
            }

            jda.getTextChannelById(settingService.get(SettingConstants.MAIN_TEXT_CHANNEL_ID))
                ?.sendMessageEmbeds(embed.build())
                ?.queue()

            reminderService.deleteAll(reminders)
        }
    }
}