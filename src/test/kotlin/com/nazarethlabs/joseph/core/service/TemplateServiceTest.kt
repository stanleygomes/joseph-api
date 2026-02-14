package com.nazarethlabs.joseph.core.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.io.ByteArrayInputStream
import java.io.InputStream

class TemplateServiceTest {
    private lateinit var templateService: TemplateService
    private lateinit var templateStreamService: TemplateStreamService

    @BeforeEach
    fun setUp() {
        templateStreamService = mock(TemplateStreamService::class.java)
        templateService = TemplateService(templateStreamService)
    }

    @Test
    fun `should compile template and return rendered html`() {
        val templateContent = "Hello, {{name}}!"
        val templatePath = "templates/test.mustache"
        val context = mapOf("name" to "World")
        val inputStream: InputStream = ByteArrayInputStream(templateContent.toByteArray())

        doReturn(inputStream).`when`(templateStreamService).openStream(templatePath)

        val result = templateService.compileTemplate(templatePath, context)
        assertEquals("Hello, World!", result.trim())
    }

    @Test
    fun `should close template stream after compiling`() {
        val templateContent = "Test"
        val templatePath = "templates/test.mustache"
        val context = mapOf<String, Any>()
        val inputStream = spy(ByteArrayInputStream(templateContent.toByteArray()))

        doReturn(inputStream).`when`(templateStreamService).openStream(templatePath)

        templateService.compileTemplate(templatePath, context)
        verify(inputStream, atLeastOnce()).close()
    }

    @Test
    fun `should instantiate TemplateService with default constructor`() {
        val service = TemplateService()
        assertNotNull(service)
    }
}
