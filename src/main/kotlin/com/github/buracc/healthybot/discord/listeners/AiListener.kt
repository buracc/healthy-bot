package com.github.buracc.healthybot.discord.listeners

import com.github.buracc.healthybot.api.openai.OpenAIClient
import com.github.buracc.healthybot.api.openai.chat.ChatMessage
import com.github.buracc.healthybot.discord.SettingConstants
import com.github.buracc.healthybot.discord.SettingConstants.AI_INITIAL_PROMPT
import com.github.buracc.healthybot.service.SettingService
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class AiListener(
    private val jda: JDA, private val openAIClient: OpenAIClient,
    private val settingService: SettingService
) : ListenerAdapter() {
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
        val replied = message.referencedMessage != null && message.referencedMessage?.author?.id == selfUser.id
        if (!mentioned && !replied) {
            return
        }

        val botContext = settingService.get(AI_INITIAL_PROMPT)
        val botName = selfUser.effectiveName
        val messages = mutableListOf(
            ChatMessage("developer", botName, botContext)
        )

        val replyChain = mutableListOf<ChatMessage>()
        var reply = message.referencedMessage
        while (reply != null) {
            val sender = reply.member
            if (sender != null) {
                replyChain.add(ChatMessage(name = sender.effectiveName, content = reply.contentDisplay))
            }

            reply = reply.referencedMessage
        }

        messages.addAll(replyChain.reversed())

        val prompt = message.contentDisplay.trim()
        if (prompt.isEmpty()) {
            return
        }

        messages.add(ChatMessage(name = member.effectiveName, content = prompt))

        message.addReaction(Emoji.fromUnicode("💭")).queue()

        val response = try {
            openAIClient.createChat(
                settingService.get(SettingConstants.AI_CHAT_MODEL),
                messages
            )
        } catch (e: Exception) {
            message.reply("An error occurred while communicating with OpenAI").queue()
            e.printStackTrace()
            return
        }

        if (response == null || response.choices.isEmpty()) {
            message.reply("OpenAI did not respond.").queue()
            return
        }

        val replyContent = response.choices[0].message.content
        if (replyContent.isEmpty()) {
            message.reply("OpenAI did not respond.").queue()
            return
        }

        message.reply(replyContent).queue()
    }
}