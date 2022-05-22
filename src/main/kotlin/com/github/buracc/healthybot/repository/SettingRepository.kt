package com.github.buracc.healthybot.repository

import com.github.buracc.healthybot.repository.entity.Setting
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SettingRepository : CrudRepository<Setting, String>