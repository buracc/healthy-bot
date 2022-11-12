package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.SettingConstants.MARKOV_COOLDOWN_SEC
import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.discord.model.NoEmbed
import com.github.buracc.healthybot.repository.MarkovRepository
import com.github.buracc.healthybot.service.SettingService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class MarkovCommandHandler(
    override val jda: JDA,
    private val markovRepository: MarkovRepository,
    private val settingService: SettingService
) : CommandHandler() {
    private var lastInvocation = Instant.EPOCH

    override fun handle(command: Command, message: Message) {
        if (Duration.between(lastInvocation, Instant.now()).seconds > settingService.getInt(MARKOV_COOLDOWN_SEC)) {
            lastInvocation = Instant.now()
            respond({
                when (command.actions.getOrNull(0)) {
                    "purge" -> purge(command)
                    else -> markov(command)
                }
            }, message)
        }
    }

    fun purge(command: Command): String {
        return markovRepository.purge(command.userId.toString()) ?: "Weed xD"
    }

    fun markov(command: Command): NoEmbed {
        val userId = command.actions.getOrNull(0)?.replace("\\D".toRegex(), "") ?: command.userId.toString()
        return NoEmbed(markovRepository.get(userId) ?: "Crack cocaine")
    }
}