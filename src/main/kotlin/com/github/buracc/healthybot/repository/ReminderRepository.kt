package com.github.buracc.healthybot.repository

import com.github.buracc.healthybot.repository.entity.Reminder
import com.github.buracc.healthybot.repository.entity.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ReminderRepository : CrudRepository<Reminder, Long> {
    fun findAllByOwner(owner: User): List<Reminder>
}