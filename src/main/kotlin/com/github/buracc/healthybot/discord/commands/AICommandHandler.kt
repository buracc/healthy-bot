package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.api.openapi.OpenAIClient
import com.github.buracc.healthybot.discord.SettingConstants
import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.helper.Utils
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
                    "chat" -> chat(command)
                    "text" -> text(command, "text-davinci-003")
                    "code" -> text(command, "code-davinci-002")
                    "image" -> image(command)
                    else -> text(command, "text-davinci-003")
                }
            }, message)
        }
    }

    private fun image(command: Command): NoEmbed {
        Utils.filterMessageExplicitWords(command.messageTrimmed)

        guild.getTextChannelById(command.channelId)
            ?.sendMessage("OpenAI is thinking...")
            ?.queue()

        val image = openAIClient.createImage(
            command.messageTrimmed.substring(command.actions.getOrNull(0)?.length ?: 0),
            command.userId.toString()
        ) ?: throw BotException("Could not retrieve response from OpenAI.")

        return NoEmbed(
            image.data.getOrNull(0)?.url ?: throw BotException("Image response was empty.")
        )
    }

    private fun chat(command: Command): NoEmbed {
        Utils.filterMessageExplicitWords(command.messageTrimmed)

        guild.getTextChannelById(command.channelId)
            ?.sendMessage("OpenAI is thinking...")
            ?.queue()

        val chat = openAIClient.createChat(
            command.messageTrimmed.substring(command.actions.getOrNull(0)?.length ?: 0),
            command.userId.toString()
        ) ?: throw BotException("Could not retrieve response from OpenAI.")

        return NoEmbed(
            chat.choices.getOrNull(0)?.message?.content ?: throw BotException("Chat response was empty.")
        )
    }

    private fun text(command: Command, model: String): NoEmbed {
        Utils.filterMessageExplicitWords(command.messageTrimmed)

        guild.getTextChannelById(command.channelId)
            ?.sendMessage("OpenAI is thinking...")
            ?.queue()

        val text = openAIClient.createText(
            command.messageTrimmed.substring(command.actions.getOrNull(0)?.length ?: 0),
            command.userId.toString(),
            model
        ) ?: throw BotException("Could not retrieve response from OpenAI.")

        return NoEmbed(
            text.choices.getOrNull(0)?.text ?: throw BotException("Text response was empty.")
        )
    }
}