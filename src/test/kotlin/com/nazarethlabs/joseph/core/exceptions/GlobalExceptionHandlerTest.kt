package com.nazarethlabs.joseph.core.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest {
    private lateinit var globalExceptionHandler: GlobalExceptionHandler

    @BeforeEach
    fun setup() {
        globalExceptionHandler = GlobalExceptionHandler()
    }

    @Test
    @DisplayName("deve manipular ResourceNotFoundException e retornar 404 Not Found com a mensagem de erro correta")
    fun `should handle ResourceNotFoundException and return 404 Not Found`() {
        val errorMessage = "Recurso com ID 123 não foi encontrado."
        val exception = ResourceNotFoundException(errorMessage)

        val responseEntity = globalExceptionHandler.handleResourceNotFound(exception)

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)

        val body = responseEntity.body
        assertNotNull(body)

        assertEquals(1, body?.size)
        assertEquals(errorMessage, body?.get("error"))
    }

    @Test
    @DisplayName("deve manipular ResourceAlreadyExistsException e retornar 409 Conflict com a mensagem de erro correta")
    fun `should handle ResourceAlreadyExistsException and return 409 Conflict`() {
        val errorMessage = "Recurso já existe."
        val exception = ResourceAlreadyExistsException(errorMessage)

        val responseEntity = globalExceptionHandler.handleResourceAlreadyExists(exception)

        assertEquals(HttpStatus.CONFLICT, responseEntity.statusCode)
        val body = responseEntity.body
        assertNotNull(body)
        assertEquals(1, body?.size)
        assertEquals(errorMessage, body?.get("error"))
    }

    @Test
    @DisplayName("deve manipular IntegrationException e retornar 502 Bad Gateway com mensagem genérica")
    fun `should handle IntegrationException and return 502 Bad Gateway`() {
        val exception = IntegrationException("Erro de integração")

        val responseEntity = globalExceptionHandler.handleIntegrationException(exception)

        assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.statusCode)
        val body = responseEntity.body
        assertNotNull(body)
        assertEquals(1, body?.size)
        assertEquals(
            "Ocorreu uma falha ao comunicar com um serviço externo. Tente novamente mais tarde.",
            body?.get("error"),
        )
    }
}
