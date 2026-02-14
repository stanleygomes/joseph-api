package com.nazarethlabs.joseph.stock

fun StockEntity.toResponse(): StockResponse =
    StockResponse(
        id = this.id!!,
        ticker = this.ticker,
        companyName = this.companyName,
    )
