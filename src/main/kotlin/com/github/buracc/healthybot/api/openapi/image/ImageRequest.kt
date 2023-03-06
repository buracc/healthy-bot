package com.github.buracc.healthybot.api.openapi.image

data class ImageRequest(
    val prompt: String,
    val user: String,
    val size: String = "1024x1024"
)