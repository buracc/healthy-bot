package com.github.buracc.healthybot.service

import com.github.buracc.healthybot.discord.exception.NotFoundException
import com.github.buracc.healthybot.repository.SettingRepository
import com.github.buracc.healthybot.repository.entity.Setting
import org.springframework.stereotype.Service

@Service
class SettingService(
    private val settingRepository: SettingRepository
) {
    fun findAll() = settingRepository.findAll()

    fun get(key: String) = settingRepository.findById(key)
        .orElseThrow { NotFoundException("Setting not found.") }
        .value

    fun getBoolean(key: String) = get(key).toBoolean()

    fun getDouble(key: String) = get(key).toDouble()

    fun getInt(key: String) = get(key).toInt()

    fun getLong(key: String) = get(key).toLong()

    fun findById(key: String) = settingRepository.findById(key)
        .orElseThrow { NotFoundException("Setting not found.") }

    fun save(setting: Setting) = settingRepository.save(setting)
}