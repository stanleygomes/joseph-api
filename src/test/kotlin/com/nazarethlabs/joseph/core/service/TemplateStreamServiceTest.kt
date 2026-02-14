package com.nazarethlabs.joseph.core.service

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.InputStream

class TemplateStreamServiceTest {
    private val templateStreamService = TemplateStreamService()

    @Test
    fun `should open stream for existing template`() {
        val templatePath = "templates/stock-quote-report.html"
        val inputStream: InputStream = templateStreamService.openStream(templatePath)
        assertNotNull(inputStream)
        inputStream.close()
    }

    @Test
    fun `should throw exception for non-existent template`() {
        val templatePath = "templates/nonexistent.mustache"
        try {
            templateStreamService.openStream(templatePath)
            assert(false) { "Expected exception for missing template" }
        } catch (ex: Exception) {
            assert(true)
        }
    }
}
