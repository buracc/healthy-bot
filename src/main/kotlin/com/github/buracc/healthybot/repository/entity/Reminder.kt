package com.github.buracc.healthybot.repository.entity

import com.github.buracc.healthybot.service.ReminderService
import java.time.ZonedDateTime
import javax.persistence.*

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
    val remindDate: ZonedDateTime
        get() = ZonedDateTime.parse(remindDateString, ReminderService.format)
}