package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.SettingConstants.MAIN_TEXT_CHANNEL_ID
import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.repository.entity.Role
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class UserCommandHandler(
    override val jda: JDA,
    private val guild: Guild,
    private val settingService: SettingService,
    private val userService: UserService
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
        respond({
            when (command.actions.getOrNull(1)) {
                "latest_message" -> latestMessage(command)
                "authorize" -> authorize(command)
                else -> "Invalid action."
            }
        }, message)
    }

    private fun authorize(command: Command): String {
        val user = userService.findByIdOrCreate(command.userId)
        if (user.role != Role.ADMIN) {
            throw UnauthorizedException("You are not authorized to use this command.")
        }

        val userId = command.actions.getOrNull(0) ?: throw BotException("No user id specified.")
        val member = userService.findByIdOrCreate(userId) ?: throw BotException("Member not found.")

        userService.save(member.also { it.authorized = !it.authorized })

        return "<@${member.discordId}> is ${if (member.authorized) "now" else "no longer"} authorized to use commands."
    }

    private fun latestMessage(command: Command): String {
        val textChannelId = settingService.get(MAIN_TEXT_CHANNEL_ID)
        val channel = guild.getTextChannelById(textChannelId) ?: throw BotException("Channel not found.")
        val userId = command.actions.getOrNull(0) ?: throw BotException("No user id specified.")
        val member = guild.getMemberById(userId) ?: throw BotException("Member not found.")

        val latestMessage = channel.history
            .retrievedHistory
            .also { println(it.map { y -> y.contentStripped }) }
            .firstOrNull { m -> m.member?.id == member.id }

            ?: return "No message found for member ${member.asMention}"

        return "${member.asMention}'s latest message was: ${latestMessage.contentStripped} at ${latestMessage.timeCreated}"
    }
}