package com.github.buracc.healthybot.repository.entity

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    var discordId: String,
    var birthday: String? = null,
    var authorized: Boolean = false,
    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,
) {
    val birthDate: LocalDate?
        get() = if (birthday == null) null else LocalDate.parse(birthday)
}