package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.SettingConstants.MAIN_TEXT_CHANNEL_ID
import com.github.buracc.healthybot.discord.exception.BotException
import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.repository.entity.Role
import com.github.buracc.healthybot.service.SettingService
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.math.abs

@Component
class UserCommandHandler(
    override val jda: JDA,
    private val settingService: SettingService,
    private val userService: UserService
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
        when (command.command) {
            "inthards" -> {
                respond({ inthards(command) }, message)
            }
            else -> {
                respond({
                    when (command.actions.getOrNull(0)) {
                        "latest_message" -> latestMessage(command)
                        "authorize" -> authorize(command)
                        "sync" -> sync(command)
                        else -> "Invalid action."
                    }
                }, message)
            }
        }
    }

    private fun sync(command: Command): String {
        val user = userService.findByIdOrCreate(command.userId)
        if (user.role != Role.ADMIN) {
            throw UnauthorizedException("You are not authorized to use this command.")
        }

        guild.loadMembers().onSuccess {
            val users = it
                .filter { member -> !member.user.isBot }
                .map { member -> userService.createIfNotExists(member.id) }
            userService.saveAll(users)
            guild.getTextChannelById(command.channelId)
                ?.sendMessage("Synced ${users.size} members with database.")
                ?.queue()
        }

        return "Syncing members with database..."
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

    private fun inthards(command: Command): EmbedBuilder {
        val topX = command.actions.getOrNull(0)?.toIntOrNull() ?: 5
        val users = userService.findAll()
            .sortedBy { it.lastMessage }
            .take(abs(topX))

        val embed = embedHelper.builder("Inthards Top List")
        embed.setDescription("Top $topX inactive inters")

        users.forEachIndexed { idx, user ->
            embed.addField(
                "${idx.plus(1)}.",
                "<@${user.discordId}>: <t:${user.lastMessage?.epochSecond ?: Instant.EPOCH.epochSecond}>",
                false
            )
        }

        return embed
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