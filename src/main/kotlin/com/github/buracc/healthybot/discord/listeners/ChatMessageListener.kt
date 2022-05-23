package com.github.buracc.healthybot.discord.listeners

import com.github.buracc.healthybot.discord.SettingConstants.CHAT_INCOME_MAX
import com.github.buracc.healthybot.discord.SettingConstants.CHAT_INCOME_MIN
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.random.Random

@Component
class ChatMessageListener(
    private val jda: JDA,
    private val userService: UserService,
    private val settingService: SettingService
) : ListenerAdapter() {
    @PostConstruct
    fun register() {
        jda.addEventListener(this)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val userId = event.member?.id ?: return
        val user = userService.findById(userId)
        val reward = Random.nextInt(
            settingService.getInt(CHAT_INCOME_MIN),
            settingService.getInt(CHAT_INCOME_MAX),
        )
        userService.save(user.also { it.cash += reward })
    }
}