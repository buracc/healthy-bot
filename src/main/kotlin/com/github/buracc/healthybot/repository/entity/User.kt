package com.github.buracc.healthybot.repository.entity

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    var discordId: String,
    var cash: Int = 0,
    var bank: Int = 0,
    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,
    var lastRob: Instant = Instant.now().minusMillis(60_000)
) {
    val balance: Int
        get() = cash + bank

    val robCooldown: Long
        get() = lastRob.toEpochMilli() - Instant.now().toEpochMilli()
}