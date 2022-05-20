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
    var robCooldown: Instant = Instant.now(),
    var workCooldown: Instant = Instant.now(),
    var crimeCooldown: Instant = Instant.now(),
    var slutCooldown: Instant = Instant.now(),
) {
    val balance: Int
        get() = cash + bank
}