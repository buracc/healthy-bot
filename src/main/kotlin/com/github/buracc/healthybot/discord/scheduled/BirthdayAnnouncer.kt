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
import javax.annotation.PostConstruct

@Component
class BirthdayAnnouncer(
    private val jda: JDA,
    private val embedHelper: EmbedHelper,
    private val userService: UserService,
    private val settingService: SettingService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        checkAndAnnounce()
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun checkAndAnnounce() {
        logger.info("Checking birthdays")
        val bdays = mutableListOf<User>()
        val now = LocalDate.now()
        userService.findAll().forEach {
            if (it.birthday == null) {
                return@forEach
            }

            try {
                val date = LocalDate.parse(it.birthday)
                if (now.dayOfMonth == date.dayOfMonth && now.monthValue == date.monthValue) {
                    bdays.add(it)
                }
            } catch (e: DateTimeParseException) {
                logger.error("Failed to parse {}'s birthday {}", it.discordId, it.birthday)
                return@forEach
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