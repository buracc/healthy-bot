package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.repository.FoodRepository
import com.github.buracc.healthybot.repository.entity.Food
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional

@Service
class FoodService(
    private val foodRepository: FoodRepository
) {
    fun findById(id: Long) = foodRepository.findById(id)
        .orElseThrow { NotFoundException("Food not found.") }

    fun findByMessageId(id: String) = foodRepository.findByMessageId(id)
        .orElseThrow { NotFoundException("Food not found.") }

    fun findAllVotable() = foodRepository.findAllByCanVoteTrue()

    @Transactional
    fun save(food: Food): Food {
        val newId = food.id
        return if (newId != null) {
            val find = findById(newId)
            foodRepository.save(food.also { it.id = find.id })
        } else {
            foodRepository.save(food)
        }
    }
}