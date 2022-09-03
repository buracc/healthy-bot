package com.github.buracc.healthybot.repository.entity

import java.time.ZonedDateTime
import javax.persistence.*

@Entity
data class Reminder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val message: String,
    val remindDate: ZonedDateTime,
    @OneToOne
    val owner: User
)