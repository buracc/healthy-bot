package com.github.buracc.healthybot.discord.model

import java.time.Duration

data class Command(
    val userId: Long,
    val command: String,
    val messageTrimmed: String,
    val actions: Array<String>,
) {
    private val cooldowns = mapOf(
        "markov" to Duration.ofSeconds(5)
    )

    val cooldown: Duration
        get() = cooldowns.getOrDefault(command, Duration.ZERO)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Command

        if (userId != other.userId) return false
        if (command != other.command) return false
        if (messageTrimmed != other.messageTrimmed) return false
        if (!actions.contentEquals(other.actions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + messageTrimmed.hashCode()
        result = 31 * result + actions.contentHashCode()
        return result
    }
}