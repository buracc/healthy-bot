package com.github.buracc.healthybot.repository.entity

import com.github.buracc.healthybot.discord.helper.Utils.parseDateTime
import jakarta.persistence.*

@Entity
data class Reminder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val message: String,
    val remindDateString: String,
    @OneToOne
    val owner: User
) {
    val remindDate
        get() = parseDateTime(remindDateString)
}