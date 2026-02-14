package com.nazarethlabs.joseph.stock

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/stocks")
@Tag(name = "Stocks", description = "API para gerenciamento de ações")
class StockController(
    private val stockService: StockService,
) {
    @Operation(
        summary = "Cria uma nova ação",
        description = "Adiciona uma nova ação à carteira.",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Ação criada com sucesso",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = StockResponse::class),
                    ),
                ],
            ),
            ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = [Content()]),
        ],
    )
    @PostMapping
    fun createStock(
        @RequestBody request: CreateStockRequest,
    ): ResponseEntity<StockResponse> {
        val stockResponse = stockService.createStock(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(stockResponse)
    }

    @Operation(
        summary = "Lista todas as ações",
        description = "Recupera uma lista com todas as ações cadastradas.",
        responses = [
            ApiResponse(responseCode = "200", description = "Lista de ações retornada com sucesso"),
        ],
    )
    @GetMapping
    fun getAllStocks(): ResponseEntity<List<StockResponse>> = ResponseEntity.ok(stockService.getAllStocks())

    @Operation(
        summary = "Busca uma ação por ID",
        description = "Recupera os dados de uma única ação pelo seu identificador único.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Ação encontrada",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = StockResponse::class),
                    ),
                ],
            ),
            ApiResponse(responseCode = "404", description = "Ação não encontrada", content = [Content()]),
        ],
    )
    @GetMapping("/{id}")
    fun getStockById(
        @PathVariable id: UUID,
    ): ResponseEntity<StockResponse> =
        ResponseEntity.ok(
            stockService.getStockById(id),
        )

    @Operation(
        summary = "Exclui uma ação",
        description = "Marca uma ação como excluída, preenchendo o campo `deletedAt`.",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Ação marcada como excluída com sucesso",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ação não encontrada",
                content = [Content()],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun deleteStock(
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        stockService.deleteStock(id)
        return ResponseEntity.noContent().build()
    }
}
