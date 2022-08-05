package com.github.buracc.healthybot.repository.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
data class FoodRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JsonIgnore
    val food: Food,
    val userId: String,
    var upvote: Boolean? = null
)