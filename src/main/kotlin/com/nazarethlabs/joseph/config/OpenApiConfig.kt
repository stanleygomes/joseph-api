package com.nazarethlabs.joseph.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${springdoc.api-docs.info.title}")
    private val title: String,
    @Value("\${springdoc.api-docs.info.version}")
    private val version: String,
    @Value("\${springdoc.api-docs.info.description}")
    private val description: String,
) {
    private fun buildApiInfo(): Info =
        Info()
            .title(title)
            .version(version)
            .description(description)

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI().info(this.buildApiInfo())
}
