package com.nazarethlabs.joseph.integration.brapi

import com.nazarethlabs.joseph.core.client.HttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class BrapiClientTest {
    @Mock
    private lateinit var httpClient: HttpClient

    @InjectMocks
    private lateinit var brapiClient: BrapiClient

    private val brapiQuoteResultResponse =
        BrapiQuoteResultResponse(
            symbol = "PETR4",
            longName = "Petrobras",
            shortName = "PETR4",
            currency = "BRL",
            regularMarketPrice = BigDecimal("10.00"),
            regularMarketOpen = BigDecimal("9.50"),
            regularMarketDayHigh = BigDecimal("10.50"),
            regularMarketDayLow = BigDecimal("9.40"),
            regularMarketVolume = 1000L,
            marketCap = 1000000L,
            logoUrl = null,
        )
    private val brapiQuoteResponse = BrapiQuoteResponse(results = listOf(brapiQuoteResultResponse))

    @Test
    fun `getQuotes deve retornar lista vazia se lista de tickers for vazia`() {
        val result = brapiClient.getQuotes(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getQuotes deve retornar lista vazia se resposta for nula`() {
        whenever(
            httpClient.get(
                path = "/quote/{tickers}",
                responseType = BrapiQuoteResponse::class.java,
                pathVariables = mapOf("tickers" to "PETR4"),
            ),
        ).thenReturn(null)

        val result = brapiClient.getQuotes(listOf("PETR4"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getQuotes deve retornar lista vazia se results for vazio`() {
        whenever(
            httpClient.get(
                path = "/quote/{tickers}",
                responseType = BrapiQuoteResponse::class.java,
                pathVariables = mapOf("tickers" to "PETR4"),
            ),
        ).thenReturn(BrapiQuoteResponse(results = emptyList()))

        val result = brapiClient.getQuotes(listOf("PETR4"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getQuotes deve retornar lista de StockQuoteQueryResponse quando houver resultados`() {
        whenever(
            httpClient.get(
                path = "/quote/{tickers}",
                responseType = BrapiQuoteResponse::class.java,
                pathVariables = mapOf("tickers" to "PETR4"),
            ),
        ).thenReturn(brapiQuoteResponse)

        val result = brapiClient.getQuotes(listOf("PETR4"))
        assertEquals(1, result.size)
        assertEquals(BigDecimal("9.50").setScale(2), result[0].openPrice)
        assertEquals(BigDecimal("10.50").setScale(2), result[0].highPrice)
        assertEquals(BigDecimal("9.40").setScale(2), result[0].lowPrice)
        assertEquals(BigDecimal("10.00").setScale(2), result[0].closePrice)
        assertEquals(1000L, result[0].volume)
    }

    @Test
    fun `getQuote deve retornar Optional vazio se não houver resultado`() {
        whenever(
            httpClient.get(
                path = "/quote/{tickers}",
                responseType = BrapiQuoteResponse::class.java,
                pathVariables = mapOf("tickers" to "PETR4"),
            ),
        ).thenReturn(BrapiQuoteResponse(results = emptyList()))

        val result = brapiClient.getQuote("PETR4")
        assertTrue(result.isEmpty)
    }

    @Test
    fun `getQuote deve retornar Optional com StockQuoteQueryResponse se houver resultado`() {
        whenever(
            httpClient.get(
                path = "/quote/{tickers}",
                responseType = BrapiQuoteResponse::class.java,
                pathVariables = mapOf("tickers" to "PETR4"),
            ),
        ).thenReturn(brapiQuoteResponse)

        val result = brapiClient.getQuote("PETR4")
        assertTrue(result.isPresent)
        assertEquals(BigDecimal("10.00").setScale(2), result.get().closePrice)
    }
}
