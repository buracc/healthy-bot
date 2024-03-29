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
import net.dv8tion.jda.api.utils.FileUpload
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.URL
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

        nonRatedFoods.forEach { food ->
            food.canVote = false
            val ratings = food.ratings.mapNotNull { it.upvote }
            val upvotes = ratings.filter { it }.size
            val downvotes = ratings.filter { !it }.size
            val ratio = upvotes.toDouble().div(downvotes)
            val image = URL(food.imageUrl).readBytes()
            val embed = embedHelper.builder()
                .setFooter("👍$upvotes 👎$downvotes")
                .setImage("attachment://image.png")

            if ((ratio > ratioSetting || downvotes == 0) && upvotes > 0) {
                embed
                    .setTitle("Food Heaven 🎂")
                    .setDescription("Proppa food lads!! Well done <@${food.ownerId}>")
                heaven
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(image, "image.png"))
                    .queue()
            } else {
                embed
                    .setTitle("Food Hell 💩")
                    .setDescription("Shit food, <@${food.ownerId}> wtf is this dog food")
                hell
                    .sendMessageEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(image, "image.png"))
                    .queue()
            }

            submissionChannel.deleteMessageById(food.messageId!!).queue()
            foodService.save(food)
        }
    }
}