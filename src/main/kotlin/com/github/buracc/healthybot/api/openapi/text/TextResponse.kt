package com.github.buracc.healthybot.api.openapi.text

import java.time.Instant

data class TextResponse(
    val created: Instant,
    val choices: List<Choice>,
) {
    data class Choice(
        val index: Int,
        val text: String
    )
}