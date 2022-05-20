package com.github.buracc.healthybot.repository.entity

import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    var discordId: String,
    var balance: Int = 0,
    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,
)