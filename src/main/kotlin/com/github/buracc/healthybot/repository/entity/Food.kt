package com.github.buracc.healthybot.repository.entity

import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.jpa.repository.Temporal
import java.time.Instant
import jakarta.persistence.*

@Entity
data class Food(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var messageId: String? = null,
    val ownerId: String,
    var imageUrl: String? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id")
    val ratings: MutableList<FoodRating> = mutableListOf(),
    var canVote: Boolean = true
) {
    @field:CreationTimestamp
    @setparam:Temporal(TemporalType.TIMESTAMP)
    lateinit var createdOn: Instant
}