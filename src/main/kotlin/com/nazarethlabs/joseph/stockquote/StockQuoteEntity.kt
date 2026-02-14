package com.nazarethlabs.joseph.stockquote

import com.fasterxml.jackson.annotation.JsonBackReference
import com.nazarethlabs.joseph.stock.StockEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "stock_quotes")
data class StockQuoteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    @JsonBackReference
    val stockEntity: StockEntity? = null,
    @Column(name = "quote_date", nullable = false)
    var quoteDate: LocalDate? = LocalDate.now(),
    @Column(name = "open_price")
    var openPrice: BigDecimal? = BigDecimal.ZERO,
    @Column(name = "high_price")
    var highPrice: BigDecimal? = BigDecimal.ZERO,
    @Column(name = "low_price")
    var lowPrice: BigDecimal? = BigDecimal.ZERO,
    @Column(name = "close_price", nullable = false)
    var closePrice: BigDecimal? = BigDecimal.ZERO,
    @Column(name = "volume")
    var volume: Long? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)
