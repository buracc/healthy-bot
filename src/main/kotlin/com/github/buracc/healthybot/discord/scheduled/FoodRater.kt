package com.github.buracc.healthybot.discord.scheduled

import com.github.buracc.healthybot.discord.SettingConstants.FOOD_CHANNEL_ID
import com.github.buracc.healthybot.discord.SettingConstants.FOOD_HEAVEN_CHANNEL_ID
import com.github.buracc.healthybot.discord.SettingConstants.FOOD_HELL_CHANNEL_ID
import com.github.buracc.healthybot.discord.SettingConstants.FOOD_VOTE_PERIOD
import com.github.buracc.healthybot.discord.SettingConstants.FOOD_VOTE_RATIO
import com.github.buracc.healthybot.discord.helper.EmbedHelper
import com.github.buracc.healthybot.service.FoodService
import com.github.buracc.healthybot.service.SettingService
import net.dv8tion.jda.api.entities.Guild
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class FoodRater(
    private val guild: Guild,
    private val embedHelper: EmbedHelper,
    private val foodService: FoodService,
    private val settingService: SettingService
) {
    @Scheduled(cron = "0 * * * * *")
    fun checkFoods() {
        val period = settingService.getLong(FOOD_VOTE_PERIOD)
        val now = Instant.now()
        val nonRatedFoods = foodService.findAllVotable()
            .filter { it.createdOn.plus(period - 1, ChronoUnit.MINUTES).isBefore(now) }
        val ratioSetting = settingService.getDouble(FOOD_VOTE_RATIO)
        val hell = guild.getTextChannelById(settingService.get(FOOD_HELL_CHANNEL_ID)) ?: return
        val heaven = guild.getTextChannelById(settingService.get(FOOD_HEAVEN_CHANNEL_ID)) ?: return
        val submissionChannel = guild.getTextChannelById(settingService.get(FOOD_CHANNEL_ID)) ?: return
        for (food in nonRatedFoods) {
            food.canVote = false
            val ratings = food.ratings.mapNotNull { it.upvote }
            val upvotes = ratings.filter { it }.size
            val downvotes = ratings.filter { !it }.size
            val ratio = upvotes.toDouble().div(downvotes)
            val embed = embedHelper.builder("👍$upvotes 👎$downvotes")
                .setAuthor("<@${food.ownerId}>")
                .setImage(food.imageUrl)
            if (ratio > ratioSetting || downvotes == 0) {
                embed.setDescription("Proppa food lads!!")
                heaven.sendMessageEmbeds(embed.build()).queue()
            } else {
                embed.setDescription("Shit food, <@${food.ownerId}> wtf is this dog food")
                hell.sendMessageEmbeds(embed.build()).queue()
            }

            submissionChannel.deleteMessageById(food.messageId!!).queue()
            foodService.save(food)
        }
    }
}