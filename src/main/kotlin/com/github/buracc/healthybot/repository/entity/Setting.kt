package com.github.buracc.healthybot.repository.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Setting(
    @Id
    val key: String,
    var value: String
)