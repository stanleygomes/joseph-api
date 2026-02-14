package com.nazarethlabs.joseph.stock

import com.nazarethlabs.joseph.core.exceptions.ResourceAlreadyExistsException
import com.nazarethlabs.joseph.core.exceptions.ResourceNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant.now
import java.util.UUID

@Service
class StockService(
    private val stockRepository: StockRepository,
) {
    fun createStock(request: CreateStockRequest): StockResponse {
        val existingStock = stockRepository.findByTickerIncludingDeleted(request.ticker)

        if (existingStock != null) {
            if (existingStock.deletedAt == null) {
                throw ResourceAlreadyExistsException("Stock with ticker '${request.ticker}' already exists.")
            } else {
                existingStock.apply {
                    this.companyName = request.companyName
                    this.deletedAt = null
                }
                val savedStock = stockRepository.save(existingStock)
                return savedStock.toResponse()
            }
        }

        val stockEntity = StockEntity(ticker = request.ticker, companyName = request.companyName)
        val savedStock = stockRepository.save(stockEntity)
        return savedStock.toResponse()
    }

    fun getAllStocks(): List<StockResponse> = stockRepository.findAll().map { it.toResponse() }

    fun getStockById(id: UUID): StockResponse {
        val stock =
            stockRepository.findByIdOrNull(id)
                ?: throw ResourceNotFoundException("Stock with ID $id not found")
        return stock.toResponse()
    }

    fun deleteStock(id: UUID) {
        val stockToDelete =
            stockRepository.findByIdOrNull(id)
                ?: throw ResourceNotFoundException("Stock with ID $id not found")

        stockToDelete.deletedAt = now()
        stockRepository.save(stockToDelete)
    }
}
