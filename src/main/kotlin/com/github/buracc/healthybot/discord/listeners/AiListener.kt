package com.github.buracc.healthybot.discord.listeners

import com.github.buracc.healthybot.api.openai.OpenAIClient
import com.github.buracc.healthybot.discord.SettingConstants
import com.github.buracc.healthybot.service.SettingService
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class AiListener(private val jda: JDA, private val openAIClient: OpenAIClient,
    private val settingService: SettingService) : ListenerAdapter() {
    @PostConstruct
    fun register() {
        jda.addEventListener(this)
    }
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val member = event.member ?: return
        val selfUser = jda.selfUser
        if (member.id == selfUser.id) {
            return
        }

        val message = event.message
        val mentioned = message.mentions.users.any { it.id == selfUser.id }
        if (!mentioned) {
            return
        }

        var context: String? = null;
        val reply = message.referencedMessage
        if (reply != null) {
            val sender = reply.author
            context = "'${sender.globalName}' said: '${reply.contentRaw}'. "
        }

        val content = message.contentRaw.replace("<@!?${selfUser.id}>", "").trim()
        if (content.isEmpty()) {
            return
        }

        val response = openAIClient.createChat(
            context ?: "",
            settingService.get(SettingConstants.AI_CHAT_MODEL),
            content,
            member.id
        )

        if (response == null || response.choices.isEmpty()) {
            event.channel.sendMessage("OpenAI did not respond.").queue()
            return
        }

        val replyContent = response.choices[0].message.content
        if (replyContent.isEmpty()) {
            event.channel.sendMessage("OpenAI did not respond.").queue()
            return
        }

        event.channel.sendMessage(replyContent).queue { msg ->
            msg.addReaction(Emoji.fromUnicode("👍")).queue()
        }
    }
}