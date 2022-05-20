package com.github.buracc.healthybot.repository

import com.github.buracc.healthybot.repository.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String>