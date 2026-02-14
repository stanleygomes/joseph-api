package com.nazarethlabs.joseph.stockquote

import com.nazarethlabs.joseph.core.port.EmailProvider
import com.nazarethlabs.joseph.core.port.StockQuoteProvider
import com.nazarethlabs.joseph.core.service.TemplateService
import com.nazarethlabs.joseph.stock.StockEntity
import com.nazarethlabs.joseph.stock.StockRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class StockQuoteServiceTest {
    private lateinit var stockQuoteService: StockQuoteService

    @Mock
    private lateinit var stockQuoteRepository: StockQuoteRepository

    @Mock
    private lateinit var stockRepository: StockRepository

    @Mock
    private lateinit var stockQuoteProvider: StockQuoteProvider

    @Mock
    private lateinit var templateService: TemplateService

    @Mock
    private lateinit var emailProvider: EmailProvider

    private val stockId = UUID.randomUUID()
    private val stockEntity = StockEntity(id = stockId, ticker = "PETR4", companyName = "Petrobras")
    private val reportRecipient = "test@example.com"

    @BeforeEach
    fun setUp() {
        stockQuoteService =
            StockQuoteService(
                stockQuoteRepository,
                stockRepository,
                stockQuoteProvider,
                templateService,
                emailProvider,
                reportRecipient,
            )
    }

    @Nested
    @DisplayName("updatePendingDayQuote")
    inner class UpdatePendingDayQuote {
        @Test
        fun `deve retornar mensagem de sucesso ao atualizar cotação pendente`() {
            val today = LocalDate.now()
            val quoteResponse =
                StockQuoteQueryResponse(
                    openPrice = BigDecimal("10.00"),
                    highPrice = BigDecimal("12.00"),
                    lowPrice = BigDecimal("9.50"),
                    closePrice = BigDecimal("11.00"),
                    volume = 1000L,
                )
            val stockQuoteEntity =
                StockQuoteEntity(
                    stockEntity = stockEntity,
                    quoteDate = today,
                    openPrice = quoteResponse.openPrice,
                    highPrice = quoteResponse.highPrice,
                    lowPrice = quoteResponse.lowPrice,
                    closePrice = quoteResponse.closePrice,
                    volume = quoteResponse.volume,
                )
            `when`(stockRepository.findAll()).thenReturn(listOf(stockEntity))
            `when`(
                stockQuoteRepository.findByStockEntityIdInAndQuoteDate(listOf(stockId), today),
            ).thenReturn(emptyList())
            `when`(stockQuoteProvider.getQuote(stockEntity.ticker)).thenReturn(Optional.of(quoteResponse))
            `when`(stockQuoteRepository.save(any())).thenReturn(stockQuoteEntity)

            val result = stockQuoteService.updatePendingDayQuote()

            assertEquals("Stock quote for PETR4 updated successfully.", result.message)
            verify(stockQuoteRepository).save(any())
        }

        @Test
        fun `deve retornar mensagem quando não há ação pendente para cotação`() {
            whenever(stockRepository.findAll()).thenReturn(emptyList())

            val result = stockQuoteService.updatePendingDayQuote()

            assertEquals("No stock found without quote for today.", result.message)
        }

        @Test
        fun `deve retornar mensagem quando não encontra cotação para a ação`() {
            val today = LocalDate.now()
            whenever(stockRepository.findAll()).thenReturn(listOf(stockEntity))
            whenever(
                stockQuoteRepository.findByStockEntityIdInAndQuoteDate(listOf(stockId), today),
            ).thenReturn(emptyList())
            whenever(stockQuoteProvider.getQuote(stockEntity.ticker)).thenReturn(Optional.empty())

            val result = stockQuoteService.updatePendingDayQuote()

            assertEquals("No quote found for stock: PETR4.", result.message)
        }

        @Test
        fun `deve retornar mensagem quando todas as ações já possuem cotação para hoje`() {
            val today = LocalDate.now()
            val quoteResponse =
                StockQuoteQueryResponse(
                    openPrice = BigDecimal("10.00"),
                    highPrice = BigDecimal("12.00"),
                    lowPrice = BigDecimal("9.50"),
                    closePrice = BigDecimal("11.00"),
                    volume = 1000L,
                )
            val stockQuoteEntity =
                StockQuoteEntity(
                    stockEntity = stockEntity,
                    quoteDate = today,
                    openPrice = quoteResponse.openPrice,
                    highPrice = quoteResponse.highPrice,
                    lowPrice = quoteResponse.lowPrice,
                    closePrice = quoteResponse.closePrice,
                    volume = quoteResponse.volume,
                )
            whenever(stockRepository.findAll()).thenReturn(listOf(stockEntity))
            whenever(
                stockQuoteRepository.findByStockEntityIdInAndQuoteDate(listOf(stockId), today),
            ).thenReturn(listOf(stockQuoteEntity))

            val result = stockQuoteService.updatePendingDayQuote()

            assertEquals("No stock found without quote for today.", result.message)
        }
    }

    @Nested
    @DisplayName("sendQuoteReportEmail")
    inner class SendQuoteReportEmail {
        @Test
        fun `should send report email with correct context and html`() {
            val stock = stockEntity
            val stockId = stock.id!!
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val todayQuote = StockQuoteEntity(stockEntity = stock, quoteDate = today, closePrice = BigDecimal("10.00"))
            val yesterdayQuote =
                StockQuoteEntity(stockEntity = stock, quoteDate = yesterday, closePrice = BigDecimal("8.00"))

            whenever(stockRepository.findAll()).thenReturn(listOf(stock))
            whenever(
                stockQuoteRepository.findByStockEntityIdInAndQuoteDate(listOf(stockId), today),
            ).thenReturn(listOf(todayQuote))
            whenever(
                stockQuoteRepository.findByStockEntityIdInAndQuoteDate(listOf(stockId), yesterday),
            ).thenReturn(listOf(yesterdayQuote))
            whenever(templateService.compileTemplate(any(), any())).thenReturn("<html>report</html>")

            val result = stockQuoteService.sendQuoteReportEmail(7)

            assertEquals("Report sent successfully.", result.message)
            verify(emailProvider).send(
                any(),
                any(),
                any(),
            )
            verify(templateService).compileTemplate(any(), any())
        }
    }
}
