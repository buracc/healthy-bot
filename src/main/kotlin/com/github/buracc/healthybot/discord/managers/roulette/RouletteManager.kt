package com.github.buracc.healthybot.discord.managers.roulette

import com.github.buracc.healthybot.discord.exception.CasinoException
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.service.UserService
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Component
class RouletteManager(
    private val userService: UserService,
    private val embedHelper: EmbedHelper
) {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var future: Future<*>? = null

    private val bets = mutableListOf<Bet>()

    fun bet(space: String, discordId: String, amount: Int, event: SlashCommandInteractionEvent) {
        val bet = Bet(discordId, space.lowercase(), amount)
        if (!bet.isValid()) {
            throw CasinoException("Invalid bet.")
        }

        bets.add(bet)
        val betUser = userService.findById(discordId)
        if (betUser.cash <= 0) {
            throw CasinoException("Not enough cash available to bet.")
        }
        val betAmount = if (amount > betUser.cash) betUser.cash else amount

        userService.save(betUser.also { it.cash -= betAmount })

        val task = future
        if (task == null || task.isDone) {
            future = executor.scheduleWithFixedDelay({
                val result = Random.nextInt(0, 37)
                event.replyEmbeds(embedHelper.builder("Roulette")
                    .setDescription("The ball landed on $result - ${Bet.getSpace(result)}!")
                    .build())
                    .queue()
                bets.forEach { bet ->
                    userService.save(betUser.also {
                        it.cash += bet.getRewardMultiplier(result) * bet.amount
                    })
                }
            }, 5, 0, TimeUnit.SECONDS)
        }
    }
}