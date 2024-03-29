package com.github.buracc.healthybot.discord.listeners

import com.github.buracc.healthybot.discord.SettingConstants.FOOD_CHANNEL_ID
import com.github.buracc.healthybot.repository.entity.Food
import com.github.buracc.healthybot.repository.entity.FoodRating
import com.github.buracc.healthybot.service.FoodService
import com.github.buracc.healthybot.service.SettingService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.springframework.stereotype.Component
import java.net.URL
import jakarta.annotation.PostConstruct
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

@Component
class FoodChatListener(
    private val jda: JDA,
    private val settingService: SettingService,
    private val foodService: FoodService
) : ListenerAdapter() {
    @PostConstruct
    fun register() {
        jda.addEventListener(this)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val member = event.member ?: return
        val message = event.message
        val channel = message.channel
        if (channel.id != settingService.get(FOOD_CHANNEL_ID)) {
            return
        }

        if (member.id == jda.selfUser.id) {
            val foodId = message.contentRaw
                .substringBefore(" ")
                .toLong()
            val postedFood = foodService.findById(foodId)
            foodService.save(postedFood.also {
                it.messageId = message.id
            })
            message
                .editMessage("Vote lads")
                .setActionRow(
                    Button.primary("upvote", Emoji.fromUnicode("U+2B06")),
                    Button.primary("downvote", Emoji.fromUnicode("U+2B07")),
                    Button.primary("remove", Emoji.fromUnicode("U+274C")),
                )
                .queue()
            return
        }

        val attachment = message.attachments.firstOrNull()
        if (attachment == null || !attachment.isImage) {
            message.delete().queue()
            return
        }

        val food = foodService.save(
            Food(
                ownerId = member.id,
                imageUrl = attachment.url
            )
        )

        val send = MessageCreateBuilder()
            .addContent("${food.id} ")
            .addContent("attachment://image.png")
            .build()

        val image = URL(attachment.url).readBytes()

        channel.sendMessage(send)
            .addFiles(FileUpload.fromData(image, "image.png"))
            .queue { food.imageUrl = it.attachments.firstOrNull()?.url }

        message.delete().queue()
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val messageId = event.messageId
        val action = event.button.id ?: return
        val food = foodService.findByMessageId(messageId)
        val rating = food.ratings.firstOrNull { it.userId == event.user.id }
            ?: FoodRating(food = food, userId = event.user.id)

        when (action) {
            "upvote" -> rating.upvote = true
            "downvote" -> rating.upvote = false
            "remove" -> rating.upvote = null
        }

        if (!food.ratings.contains(rating)) {
            food.ratings.add(rating)
        }

        foodService.save(food)
        event.reply(if (rating.upvote == null) "Removed vote" else "Successfully voted")
            .setEphemeral(true)
            .queue()
    }
}