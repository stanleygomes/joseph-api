package com.nazarethlabs.joseph.stock

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StockRepository : JpaRepository<StockEntity, UUID> {
    @Query(value = "SELECT * FROM stocks WHERE ticker = :ticker", nativeQuery = true)
    fun findByTickerIncludingDeleted(ticker: String): StockEntity?
}
