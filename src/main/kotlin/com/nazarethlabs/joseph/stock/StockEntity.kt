package com.nazarethlabs.joseph.stock

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "stocks")
@SQLRestriction(value = "deleted_at IS NULL")
data class StockEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(nullable = false, unique = true)
    var ticker: String = "",
    @Column(nullable = false)
    var companyName: String = "",
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,
)
