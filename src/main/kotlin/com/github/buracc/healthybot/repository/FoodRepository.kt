package com.github.buracc.healthybot.repository

import com.github.buracc.healthybot.repository.entity.Food
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FoodRepository : CrudRepository<Food, Long> {
    fun findByMessageId(id: String): Optional<Food>

    fun findAllByCanVoteTrue(): List<Food>
}