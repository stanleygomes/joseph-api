package com.nazarethlabs.joseph.stockquote

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nazarethlabs.joseph.core.dto.DefaultResponseDto
import com.nazarethlabs.joseph.core.exceptions.GlobalExceptionHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class StockQuoteControllerTest {
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var stockQuoteService: StockQuoteService

    @InjectMocks
    private lateinit var stockQuoteController: StockQuoteController

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun setup() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(stockQuoteController)
                .setControllerAdvice(GlobalExceptionHandler())
                .build()
    }

    @Test
    fun `deve atualizar cotação pendente e retornar 201 Created`() {
        val response = DefaultResponseDto(message = "Stock quote for PETR4 updated successfully.")
        whenever(stockQuoteService.updatePendingDayQuote()).thenReturn(response)

        mockMvc
            .perform(
                post("/v1/stock-quotes/update-pending-day-quote")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").value(response.message))
    }

    @Test
    fun `deve enviar relatório por email e retornar 201 Created`() {
        val response = DefaultResponseDto(message = "Email report sent successfully.")
        whenever(stockQuoteService.sendQuoteReportEmail()).thenReturn(response)

        mockMvc
            .perform(
                post("/v1/stock-quotes/send-report-email")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").value(response.message))
    }
}
