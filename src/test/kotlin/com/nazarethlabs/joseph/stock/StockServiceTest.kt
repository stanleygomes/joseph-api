package com.nazarethlabs.joseph.stock

import com.nazarethlabs.joseph.core.exceptions.ResourceAlreadyExistsException
import com.nazarethlabs.joseph.core.exceptions.ResourceNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class StockServiceTest {
    @Mock
    private lateinit var stockRepository: StockRepository

    @InjectMocks
    private lateinit var stockService: StockService

    @Nested
    @DisplayName("createStock")
    inner class CreateStock {
        @Test
        fun `deve criar uma nova ação quando o ticker não existir`() {
            val request = CreateStockRequest(ticker = "MGLU3", companyName = "Magazine Luiza")
            val newStockEntity =
                StockEntity(id = UUID.randomUUID(), ticker = request.ticker, companyName = request.companyName)

            `when`(stockRepository.findByTickerIncludingDeleted(request.ticker)).thenReturn(null)
            `when`(stockRepository.save(any())).thenReturn(newStockEntity)

            val result = stockService.createStock(request)

            assertEquals(newStockEntity.id, result.id)
            assertEquals("MGLU3", result.ticker)
            verify(stockRepository).findByTickerIncludingDeleted(request.ticker)
            verify(stockRepository).save(any())
        }

        @Test
        fun `deve lançar ResourceAlreadyExistsException quando o ticker já existir e estiver ativo`() {
            val request = CreateStockRequest(ticker = "PETR4", companyName = "Petrobras Nova")
            val activeStockEntity =
                StockEntity(
                    id = UUID.randomUUID(),
                    ticker = "PETR4",
                    companyName = "Petrobras Antiga",
                    deletedAt = null,
                )

            `when`(stockRepository.findByTickerIncludingDeleted(request.ticker)).thenReturn(activeStockEntity)

            val exception =
                assertThrows<ResourceAlreadyExistsException> {
                    stockService.createStock(request)
                }
            assertEquals("Stock with ticker 'PETR4' already exists.", exception.message)

            verify(stockRepository, never()).save(any())
        }

        @Test
        fun `deve reativar e atualizar uma ação quando o ticker já existir e estiver deletado`() {
            val request = CreateStockRequest(ticker = "OIBR3", companyName = "Oi S.A. (Nova)")
            val deletedStockEntity =
                StockEntity(
                    id = UUID.randomUUID(),
                    ticker = "OIBR3",
                    companyName = "Oi (Antiga)",
                    deletedAt = Instant.now(),
                )

            `when`(stockRepository.findByTickerIncludingDeleted(request.ticker)).thenReturn(deletedStockEntity)
            `when`(stockRepository.save(any())).thenAnswer { it.getArgument(0) }

            val result = stockService.createStock(request)

            val stockEntityCaptor = ArgumentCaptor.forClass(StockEntity::class.java)
            verify(stockRepository).save(stockEntityCaptor.capture())
            val savedStock = stockEntityCaptor.value

            assertNull(savedStock.deletedAt, "A data de deleção deve ser nula (reativada)")
            assertEquals("Oi S.A. (Nova)", savedStock.companyName, "O nome da empresa deve ser atualizado")
            assertEquals(deletedStockEntity.id, savedStock.id, "O ID deve ser o mesmo do registro existente")

            assertEquals(deletedStockEntity.id, result.id)
            assertEquals("Oi S.A. (Nova)", result.companyName)
        }
    }

    @Nested
    @DisplayName("getAllStocks")
    inner class GetAllStocks {
        @Test
        fun `deve retornar uma lista de ações quando houver ações cadastradas`() {
            val stocksFromRepos =
                listOf(
                    StockEntity(id = UUID.randomUUID(), ticker = "PETR4", companyName = "Petrobras"),
                    StockEntity(id = UUID.randomUUID(), ticker = "VALE3", companyName = "Vale"),
                )
            whenever(stockRepository.findAll()).thenReturn(stocksFromRepos)

            val result = stockService.getAllStocks()

            assertEquals(2, result.size, "A lista de resultados deve conter 2 ações")
            assertEquals("PETR4", result[0].ticker)
            assertEquals("Vale", result[1].companyName)
            assertEquals(stocksFromRepos[0].id, result[0].id)
            assertEquals(stocksFromRepos[1].id, result[1].id)

            verify(stockRepository).findAll()
        }

        @Test
        fun `deve retornar uma lista vazia quando não houver ações cadastradas`() {
            whenever(stockRepository.findAll()).thenReturn(emptyList())

            val result = stockService.getAllStocks()

            assertEquals(0, result.size, "A lista de resultados deve estar vazia")
            verify(stockRepository).findAll()
        }
    }

    @Nested
    @DisplayName("getStockById")
    inner class GetStockById {
        @Test
        fun `deve retornar uma ação quando o ID existir`() {
            val stockId = UUID.randomUUID()
            val stockEntity = StockEntity(id = stockId, ticker = "ITSA4", companyName = "Itaúsa")
            whenever(stockRepository.findById(stockId)).thenReturn(Optional.of(stockEntity))

            val result = stockService.getStockById(stockId)

            assertEquals(stockId, result.id)
            assertEquals("ITSA4", result.ticker)
        }

        @Test
        fun `deve lançar ResourceNotFoundException quando o ID não existir`() {
            val nonExistentId = UUID.randomUUID()
            whenever(stockRepository.findById(nonExistentId)).thenReturn(Optional.empty())

            val exception =
                assertThrows<ResourceNotFoundException> {
                    stockService.getStockById(nonExistentId)
                }
            assertEquals("Stock with ID $nonExistentId not found", exception.message)
        }
    }

    @Nested
    @DisplayName("deleteStock")
    inner class DeleteStock {
        @Test
        fun `deve marcar uma ação como deletada quando o ID existir`() {
            val stockId = UUID.randomUUID()
            val stockEntity = StockEntity(id = stockId, ticker = "BBDC4", companyName = "Bradesco")
            whenever(stockRepository.findById(stockId)).thenReturn(Optional.of(stockEntity))

            stockService.deleteStock(stockId)

            val stockEntityCaptor = ArgumentCaptor.forClass(StockEntity::class.java)
            verify(stockRepository).save(stockEntityCaptor.capture())

            val savedStock = stockEntityCaptor.value
            assertNotNull(savedStock.deletedAt)
            assertEquals(stockId, savedStock.id)
        }

        @Test
        fun `deve lançar ResourceNotFoundException ao tentar deletar um ID que não existe`() {
            val nonExistentId = UUID.randomUUID()
            whenever(stockRepository.findById(nonExistentId)).thenReturn(Optional.empty())

            assertThrows<ResourceNotFoundException> {
                stockService.deleteStock(nonExistentId)
            }
        }
    }
}
