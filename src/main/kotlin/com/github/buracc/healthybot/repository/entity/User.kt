package com.github.buracc.healthybot.repository.entity

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
) {
    val balance: Int
        get() = cash + bank
}