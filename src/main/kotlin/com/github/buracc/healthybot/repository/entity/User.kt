package com.github.buracc.healthybot.repository.entity

import java.time.LocalDate
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    var discordId: String,
    var birthday: String? = null,
    var authorized: Boolean = false,
    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,
    var lastMessage: Instant? = null,
) {
    val birthDate: LocalDate?
        get() = if (birthday == null) null else LocalDate.parse(birthday)
}