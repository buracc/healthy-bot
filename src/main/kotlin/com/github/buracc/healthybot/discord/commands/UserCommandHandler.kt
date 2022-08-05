package com.github.buracc.healthybot.discord.commands

import com.github.buracc.healthybot.discord.model.Command
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import org.springframework.stereotype.Component

@Component
class UserCommandHandler(
    override val jda: JDA
) : CommandHandler() {
    override fun handle(command: Command, message: Message) {
    }
}