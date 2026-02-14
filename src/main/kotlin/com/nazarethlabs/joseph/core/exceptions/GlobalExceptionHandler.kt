package com.nazarethlabs.joseph.core.exceptions

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<Map<String, String?>> {
        val body = mapOf("error" to ex.message)
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExists(ex: ResourceAlreadyExistsException): ResponseEntity<Map<String, String?>> {
        val body = mapOf("error" to ex.message)
        return ResponseEntity(body, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(ex: IntegrationException): ResponseEntity<Map<String, String?>> {
        logger.error("Downstream integration error", ex)

        val body =
            mapOf("error" to "Ocorreu uma falha ao comunicar com um serviço externo. Tente novamente mais tarde.")
        return ResponseEntity(body, HttpStatus.BAD_GATEWAY)
    }
}
