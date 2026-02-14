package com.nazarethlabs.joseph.stockquote

import com.nazarethlabs.joseph.core.dto.DefaultResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/stock-quotes")
@Tag(name = "Stock Quotes", description = "API para gerenciamento de cotações de ações")
class StockQuoteController(
    private val stockQuoteService: StockQuoteService,
) {
    @Operation(
        summary = "Atualiza ou cria uma cotação pendente do dia",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Cotação gerada com sucesso",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = DefaultResponseDto::class),
                    ),
                ],
            ),
            ApiResponse(responseCode = "502", description = "Erro na comunicação com o parceiro"),
        ],
    )
    @PostMapping("/update-pending-day-quote")
    fun updatePendingDayQuote(): ResponseEntity<DefaultResponseDto> {
        val response = stockQuoteService.updatePendingDayQuote()
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "Envia email com relatório de cotações de ontem vs hoje",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Relatório enviado com sucesso",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = DefaultResponseDto::class),
                    ),
                ],
            ),
            ApiResponse(responseCode = "502", description = "Erro no envio de email"),
        ],
    )
    @PostMapping("/send-report-email")
    fun sendQuoteReportEmail(): ResponseEntity<DefaultResponseDto> {
        val response = stockQuoteService.sendQuoteReportEmail()
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
