package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.repository.UserRepository
import com.github.buracc.healthybot.repository.entity.User
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun findAll() = userRepository.findAll()

    fun findByIdOrCreate(id: Long) = findByIdOrCreate(id.toString())

    fun findByIdOrCreate(id: String) = userRepository.findById(id).orElseGet {
        save(User(id))
    }

    fun createIfNotExists(id: String) = userRepository.findById(id).orElseGet {
        User(id)
    }

    fun deleteIfExists(id: String) = userRepository.findById(id).ifPresent {
        userRepository.delete(it)
    }

    fun save(user: User) = userRepository.save(user)

    fun clear() = userRepository.deleteAll()

    fun saveAll(users: Iterable<User>) = userRepository.saveAll(users)
}