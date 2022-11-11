package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.model.Command
import com.github.buracc.healthybot.discord.model.NoEmbed
import com.github.buracc.healthybot.repository.MarkovRepository
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class MarkovCommandHandler(
    override val jda: JDA,
    private val markovRepository: MarkovRepository
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
        respond({
            when (command.actions.getOrNull(0)) {
                "purge" -> purge(command)
                else -> markov(command)
            }
        }, message)
    }

    fun purge(command: Command): String {
        return markovRepository.purge(command.userId.toString()) ?: "Weed xD"
    }

    fun markov(command: Command): NoEmbed {
        val userId = command.actions.getOrNull(0)?.replace("\\D".toRegex(), "") ?: command.userId.toString()
        return NoEmbed(markovRepository.get(userId) ?: "Crack cocaine")
    }
}