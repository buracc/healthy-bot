package com.github.buracc.healthybot.discord.listeners

import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

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
        if (!content.contains("https://twitter.com") || message.embeds.isNotEmpty()) {
            return
        }

        val newContent = content
            .replace("https://twitter.com", "https://fxtwitter.com")
        val newMessage = "${message.author.name}: $newContent"

        message.delete().queue()
        message.channel.sendMessage(newMessage).queue()
    }
}