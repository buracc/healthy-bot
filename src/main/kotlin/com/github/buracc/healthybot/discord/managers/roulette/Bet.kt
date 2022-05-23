package com.github.buracc.healthybot.discord.managers.roulette

import com.github.buracc.healthybot.discord.exception.CasinoException

data class Bet(
    val discordId: String,
    val space: String,
    val amount: Int
) {
    companion object {
        val redSpaces = listOf(32, 19, 21, 25, 34, 27, 36, 30, 23, 5, 16, 1, 14, 9, 18, 7, 12, 3)
        val blackSpaces = listOf(15, 4, 2, 17, 6, 13, 11, 8, 10, 24, 33, 20, 31, 22, 29, 28, 35, 26)

        fun getSpace(number: Int): String {
            if (redSpaces.contains(number)) {
                return "red"
            }

            if (blackSpaces.contains(number)) {
                return "black"
            }

            throw CasinoException("Couldn't determine space for number $number.")
        }
    }

    fun isValid(): Boolean {
        if (space == "red" || space == "black") {
            return true
        }

        val number = space.toIntOrNull()
        return number != null && number in 0..36
    }

    fun getRewardMultiplier(result: Int): Int {
        return when (space) {
            "red" -> if (redSpaces.contains(result)) 2 else 0
            "black" -> if (blackSpaces.contains(result)) 2 else 0
            else -> {
                val betNumber = space.toInt()
                if (result == betNumber) {
                    return 36
                } else {
                    return 0
                }
            }
        }
    }
}