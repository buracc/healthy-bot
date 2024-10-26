package com.github.buracc.healthybot.repository.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
data class Reminder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val message: String,
    val date: Instant,
    @ManyToOne
    val owner: User
)