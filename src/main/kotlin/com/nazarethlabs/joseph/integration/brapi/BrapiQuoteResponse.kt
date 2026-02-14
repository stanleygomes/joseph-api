package com.nazarethlabs.joseph.integration.brapi

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BrapiQuoteResponse(
    val results: List<BrapiQuoteResultResponse>,
)

data class BrapiQuoteResultResponse(
    @JsonProperty("symbol")
    val symbol: String,
    @JsonProperty("longName")
    val longName: String?,
    @JsonProperty("shortName")
    val shortName: String?,
    @JsonProperty("currency")
    val currency: String?,
    @JsonProperty("regularMarketPrice")
    val regularMarketPrice: BigDecimal?,
    @JsonProperty("regularMarketOpen")
    val regularMarketOpen: BigDecimal?,
    @JsonProperty("regularMarketDayHigh")
    val regularMarketDayHigh: BigDecimal?,
    @JsonProperty("regularMarketDayLow")
    val regularMarketDayLow: BigDecimal?,
    @JsonProperty("regularMarketVolume")
    val regularMarketVolume: Long?,
    @JsonProperty("marketCap")
    val marketCap: Long?,
    @JsonProperty("logourl")
    val logoUrl: String?,
)
