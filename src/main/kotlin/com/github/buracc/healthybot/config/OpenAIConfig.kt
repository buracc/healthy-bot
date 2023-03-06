package com.github.buracc.healthybot.config

import com.github.buracc.healthybot.config.properties.OpenAIProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableConfigurationProperties(OpenAIProperties::class)
class OpenAIConfig(private val openAIProperties: OpenAIProperties) {
    @Bean
    fun openAIRestTemplate() = RestTemplateBuilder()
        .rootUri("https://api.openai.com/v1")
        .defaultHeader("Authorization", "Bearer ${openAIProperties.secret}")
        .defaultHeader("Content-Type", "application/json")
        .setConnectTimeout(Duration.ofMillis(openAIProperties.timeout))
        .setReadTimeout(Duration.ofMillis(openAIProperties.timeout))
        .build()
}