package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.discord.exception.UnauthorizedException
import com.github.buracc.healthybot.repository.UserRepository
import com.github.buracc.healthybot.repository.entity.User
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun findById(id: String) = userRepository.findById(id).orElseThrow { UnauthorizedException("User not found.") }

    fun findTop10() = userRepository.findAll()
        .sortedBy { it.balance }
        .take(10)

    fun save(user: User) = userRepository.save(user)

    fun saveAll(users: Iterable<User>) = userRepository.saveAll(users)
}