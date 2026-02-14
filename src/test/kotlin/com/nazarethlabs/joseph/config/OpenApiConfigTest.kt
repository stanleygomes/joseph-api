package com.nazarethlabs.joseph.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenApiConfigTest {
    @Test
    fun `customOpenAPI deve criar o bean OpenAPI com as informações corretas`() {
        val expectedTitle = "Joseph API"
        val expectedVersion = "v1.0.0"
        val expectedDescription = "API para gestão de finanças pessoais."

        val openApiConfig =
            OpenApiConfig(
                title = expectedTitle,
                version = expectedVersion,
                description = expectedDescription,
            )

        val resultOpenAPI = openApiConfig.customOpenAPI()

        assertThat(resultOpenAPI).isNotNull
        val info = resultOpenAPI.info

        assertThat(info).isNotNull
        assertThat(info.title).isEqualTo(expectedTitle)
        assertThat(info.version).isEqualTo(expectedVersion)
        assertThat(info.description).isEqualTo(expectedDescription)
    }
}
