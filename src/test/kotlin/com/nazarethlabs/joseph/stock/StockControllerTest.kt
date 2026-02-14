package com.nazarethlabs.joseph.stock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nazarethlabs.joseph.core.exceptions.GlobalExceptionHandler
import com.nazarethlabs.joseph.core.exceptions.ResourceNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class StockControllerTest {
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var stockService: StockService

    @InjectMocks
    private lateinit var stockController: StockController

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun setup() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(stockController)
                .setControllerAdvice(GlobalExceptionHandler())
                .build()
    }

    private val baseUrl = "/v1/stocks"

    @Nested
    @DisplayName("POST /v1/stocks")
    inner class CreateStock {
        @Test
        fun `deve criar uma ação e retornar 201 Created`() {
            val request = CreateStockRequest(ticker = "PETR4", companyName = "Petrobras")
            val response = StockResponse(id = UUID.randomUUID(), ticker = "PETR4", companyName = "Petrobras")
            whenever(stockService.createStock(any())).thenReturn(response)

            mockMvc
                .perform(
                    post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(response.id.toString()))
                .andExpect(jsonPath("$.ticker").value("PETR4"))

            verify(stockService).createStock(any())
        }
    }

    @Nested
    @DisplayName("GET /v1/stocks")
    inner class GetAllStocks {
        @Test
        fun `deve retornar uma lista de ações e status 200 OK`() {
            val stocks =
                listOf(
                    StockResponse(id = UUID.randomUUID(), ticker = "PETR4", companyName = "Petrobras"),
                    StockResponse(id = UUID.randomUUID(), ticker = "VALE3", companyName = "Vale"),
                )
            whenever(stockService.getAllStocks()).thenReturn(stocks)

            mockMvc
                .perform(get(baseUrl))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[1].ticker").value("VALE3"))
        }
    }

    @Nested
    @DisplayName("GET /v1/stocks/{id}")
    inner class GetStockById {
        @Test
        fun `deve retornar uma ação pelo ID e status 200 OK`() {
            val stockId = UUID.randomUUID()
            val response = StockResponse(id = stockId, ticker = "ITUB4", companyName = "Itaú Unibanco")
            whenever(stockService.getStockById(stockId)).thenReturn(response)

            mockMvc
                .perform(get("$baseUrl/$stockId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(stockId.toString()))
        }

        @Test
        fun `deve retornar 404 Not Found quando a ação não existir`() {
            val nonExistentId = UUID.randomUUID()
            whenever(stockService.getStockById(nonExistentId)).thenThrow(ResourceNotFoundException("Stock not found"))

            mockMvc
                .perform(get("$baseUrl/$nonExistentId"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("DELETE /v1/stocks/{id}")
    inner class DeleteStock {
        @Test
        fun `deve deletar uma ação e retornar 204 No Content`() {
            val stockId = UUID.randomUUID()
            doNothing().whenever(stockService).deleteStock(stockId)

            mockMvc
                .perform(delete("$baseUrl/$stockId"))
                .andExpect(status().isNoContent)

            verify(stockService).deleteStock(stockId)
        }

        @Test
        fun `deve retornar 404 Not Found ao tentar deletar uma ação que não existe`() {
            val nonExistentId = UUID.randomUUID()
            whenever(stockService.deleteStock(nonExistentId)).thenThrow(ResourceNotFoundException("Stock not found"))

            mockMvc
                .perform(delete("$baseUrl/$nonExistentId"))
                .andExpect(status().isNotFound)
        }
    }
}
