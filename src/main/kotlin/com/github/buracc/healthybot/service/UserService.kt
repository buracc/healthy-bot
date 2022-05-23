package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.repository.UserRepository
import com.github.buracc.healthybot.repository.entity.User
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun findAll() = userRepository.findAll()

    fun findById(id: String) = userRepository.findById(id).orElse(User(id))

    fun findTop10() = userRepository.findAll()
        .sortedByDescending { it.balance }
        .take(10)

    fun save(user: User) = userRepository.save(user)

    fun clear() = userRepository.deleteAll()

    fun saveAll(users: Iterable<User>) = userRepository.saveAll(users)
}