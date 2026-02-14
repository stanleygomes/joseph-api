package com.nazarethlabs.joseph.stockquote

import java.math.BigDecimal

data class StockQuoteQueryResponse(
    val openPrice: BigDecimal?,
    val highPrice: BigDecimal?,
    val lowPrice: BigDecimal?,
    val closePrice: BigDecimal?,
    val volume: Long?,
)

data class StockQuoteDayComparisonDto(
    val ticker: String,
    val companyName: String,
    val todayQuote: BigDecimal?,
    val yesterdayQuote: BigDecimal?,
)

data class StockReportItem(
    val ticker: String,
    val companyName: String,
    val todayQuote: String,
    val yesterdayQuote: String,
    val percentChange: String,
    val changeClass: String,
)
