package com.github.buracc.healthybot.api.openai.image

import java.time.Instant

data class ImageResponse(
    val created: Instant,
    val data: List<Data>
) {
    data class Data(
        val url: String
    )
}