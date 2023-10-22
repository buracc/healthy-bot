package com.github.buracc.healthybot.discord.listeners

import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component
import java.net.URL

@Component
class TwitterLinkFixer(
    val jda: JDA
) : ListenerAdapter() {
    @PostConstruct
    fun register() {
        jda.addEventListener(this)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message
        val content = message.contentRaw
        val newContent = content.replace("twitter.com", "fxtwitter.com")

        val url = URL(newContent)
        if (url.host == "fxtwitter.com") {
            message.delete().queue()
            message.channel.sendMessage(newContent).queue()
        }
    }
}