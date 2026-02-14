package com.nazarethlabs.joseph.integration.brapi

import com.nazarethlabs.joseph.core.client.HttpClient
import com.nazarethlabs.joseph.core.port.StockQuoteProvider
import com.nazarethlabs.joseph.stockquote.StockQuoteQueryResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class BrapiClient(
    @Qualifier("brapiHttpClient")
    private val client: HttpClient,
) : StockQuoteProvider {
    private val logger = LoggerFactory.getLogger(BrapiClient::class.java)

    override fun getQuotes(tickers: List<String>): List<StockQuoteQueryResponse> {
        if (tickers.isEmpty()) {
            return emptyList()
        }

        val tickersParam = tickers.joinToString(separator = ",")

        val response =
            client.get(
                path = "/quote/{tickers}",
                responseType = BrapiQuoteResponse::class.java,
                pathVariables = mapOf("tickers" to tickersParam),
            )

        if (response == null || response.results.isEmpty()) {
            logger.info("No quotes found for tickers: $tickersParam")
            return emptyList()
        }

        return response.results.map { result -> result.toCore() }
    }

    override fun getQuote(ticker: String): Optional<StockQuoteQueryResponse> =
        getQuotes(listOf(ticker))
            .firstOrNull()
            .let { response ->
                Optional.ofNullable(response)
            }
}
