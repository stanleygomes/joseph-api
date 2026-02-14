package com.nazarethlabs.joseph.core.port

import com.nazarethlabs.joseph.stockquote.StockQuoteQueryResponse
import java.util.Optional

interface StockQuoteProvider {
    fun getQuotes(tickers: List<String>): List<StockQuoteQueryResponse>

    fun getQuote(ticker: String): Optional<StockQuoteQueryResponse>
}
