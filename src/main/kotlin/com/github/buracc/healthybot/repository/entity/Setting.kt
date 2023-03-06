package com.github.buracc.healthybot.repository.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Setting(
    @Id
    val key: String,
    var value: String
)