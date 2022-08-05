package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.SettingConstants.MAIN_TEXT_CHANNEL_ID
import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.service.SettingService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageHistory
import org.springframework.stereotype.Component

@Component
class UserCommandHandler(
    override val jda: JDA,
    private val guild: Guild,
    private val settingService: SettingService
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
        respond({
            when (command.actions.getOrNull(1)) {
                "latest_message" -> latestMessage(command)
                else -> "Invalid action."
            }
        }, message)
    }

    private fun latestMessage(command: Command): String {
        val textChannelId = settingService.get(MAIN_TEXT_CHANNEL_ID)
        val channel = guild.getTextChannelById(textChannelId) ?: throw BotException("Channel not found.")
        val userId = command.actions.getOrNull(0) ?: throw BotException("No user id specified.")
        val member = guild.retrieveMemberById(userId).complete() ?: throw BotException("Member not found.")

        val latestMessage = MessageHistory.getHistoryFromBeginning(channel)
            .complete()
            .retrievedHistory
            .filter { m -> m.member == member }
            .lastOrNull() ?: return "No message found for member ${member.asMention}"

        return "${member.asMention}'s latest message was: ${latestMessage.contentStripped} at ${latestMessage.timeCreated}"
    }
}