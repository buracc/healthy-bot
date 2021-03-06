package com.github.buracc.healthybot.discord.scheduled

import com.github.buracc.healthybot.discord.SettingConstants.BDAY_CHANNEL_ID
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.repository.entity.User
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Component
class BirthdayAnnouncer(
    private val jda: JDA,
    private val embedHelper: EmbedHelper,
    private val userService: UserService,
    private val settingService: SettingService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 0 * * *")
    fun checkAndAnnounce() {
        logger.info("Checking birthdays")
        val bdays = mutableListOf<User>()
        val now = LocalDate.now()
        for (user in userService.findAll()) {
            if (user.birthday == null) {
                continue
            }

            try {
                val date = LocalDate.parse(user.birthday)
                if (now.dayOfMonth == date.dayOfMonth && now.monthValue == date.monthValue) {
                    bdays.add(user)
                }
            } catch (e: DateTimeParseException) {
                logger.error("Failed to parse {}'s birthday {}", user.discordId, user.birthday)
            }
        }

        if (bdays.isEmpty()) {
            logger.info("No bdays today")
            return
        }

        val embed = embedHelper.builder("Birthdays")
        embed.setDescription("Appa burday to u!!!")
        bdays.forEach {
            embed.addField(jda.getUserById(it.discordId)?.asTag, it.birthday, false)
        }

        jda.getTextChannelById(settingService.get(BDAY_CHANNEL_ID))
            ?.sendMessageEmbeds(embed.build())
            ?.queue()
    }
}