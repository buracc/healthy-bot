package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.api.openai.OpenAIClient
import com.github.buracc.healthybot.discord.SettingConstants
import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.discord.model.NoEmbed
import com.github.buracc.healthybot.service.SettingService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class AICommandHandler(
    override val jda: JDA,
    private val settingService: SettingService,
    private val openAIClient: OpenAIClient
) : CommandHandler() {
    private var lastInvocation = Instant.EPOCH

    override fun handle(command: Command, message: Message) {
        if (Duration.between(lastInvocation, Instant.now()).seconds > settingService.getInt(SettingConstants.AI_COOLDOWN_SEC)) {
            lastInvocation = Instant.now()
            respond({
                when (command.actions.getOrNull(0)) {
                    "image" -> image(command)
                    else -> chat(command)
                }
            }, message)
        }
    }

    private fun image(command: Command): NoEmbed {
        guild.getTextChannelById(command.channelId)
            ?.sendMessage("OpenAI is thinking...")
            ?.queue()

        val image = openAIClient.createImage(
            settingService.get(SettingConstants.AI_INITIAL_PROMPT),
            command.messageTrimmed.substring(command.actions.getOrNull(0)?.length ?: 0),
            command.userId.toString()
        ) ?: throw BotException("Could not retrieve response from OpenAI.")

        return NoEmbed(
            image.data.getOrNull(0)?.url ?: throw BotException("Image response was empty.")
        )
    }

    private fun chat(command: Command): NoEmbed {
        guild.getTextChannelById(command.channelId)
            ?.sendMessage("OpenAI is thinking...")
            ?.queue()

        val chat = openAIClient.createChat(
            settingService.get(SettingConstants.AI_INITIAL_PROMPT),
            settingService.get(SettingConstants.AI_CHAT_MODEL),
            command.messageTrimmed.substring(command.actions.getOrNull(0)?.length ?: 0),
            command.userId.toString(),
        ) ?: throw BotException("Could not retrieve response from OpenAI.")

        return NoEmbed(
            chat.choices.getOrNull(0)?.message?.content ?: throw BotException("Chat response was empty.")
        )
    }
}