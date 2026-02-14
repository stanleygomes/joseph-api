package com.nazarethlabs.joseph.stockquote

import com.nazarethlabs.joseph.core.dto.DefaultResponseDto
import com.nazarethlabs.joseph.core.port.EmailProvider
import com.nazarethlabs.joseph.core.port.StockQuoteProvider
import com.nazarethlabs.joseph.core.service.TemplateService
import com.nazarethlabs.joseph.stock.StockEntity
import com.nazarethlabs.joseph.stock.StockRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class StockQuoteService(
    private val stockQuoteRepository: StockQuoteRepository,
    private val stockRepository: StockRepository,
    private val stockQuoteProvider: StockQuoteProvider,
    private val templateService: TemplateService,
    private val emailProvider: EmailProvider,
    @Value("\${integration.resend.report-recipient}")
    private val reportRecipient: String,
) {
    @Transactional
    fun updatePendingDayQuote(): DefaultResponseDto {
        val stockWithoutQuote =
            this.getStockPendingQuoteForToday()
                ?: return DefaultResponseDto(
                    message = "No stock found without quote for today.",
                )

        val quoteResponse =
            this.getQuoteFromProvider(stockWithoutQuote)
                ?: return DefaultResponseDto(
                    message = "No quote found for stock: ${stockWithoutQuote.ticker}.",
                )

        val stockQuote =
            this.buildStockQuote(
                stockEntity = stockWithoutQuote,
                quoteResponse = quoteResponse,
            )

        this.save(stockQuote)

        return DefaultResponseDto(
            message = "Stock quote for ${stockWithoutQuote.ticker} updated successfully.",
        )
    }

    @Transactional
    fun sendQuoteReportEmail(days: Int = 7): DefaultResponseDto {
        val stocks = this.stockRepository.findAll()
        val stockIds = stocks.mapNotNull { it.id }

        val quotesToday = this.getStockQuotesToday(stockIds)
        val quotesYesterday = this.getStockQuotesYesterday(stockIds)
        val stockQuotesDiff = this.getStockQuotesDiff(stocks, quotesToday, quotesYesterday)
        val stocksList = this.buildStocksList(stockQuotesDiff)
        val context = this.buildReportContext(days, stocksList)

        val html =
            this.templateService.compileTemplate(
                templatePath = "templates/stock-quote-report.html",
                context = context,
            )

        this.emailProvider.send(
            emailList = listOf(reportRecipient),
            subject = "Relatório de Cotações de Ações",
            htmlBody = html,
        )

        return DefaultResponseDto(message = "Report sent successfully.")
    }

    private fun buildStocksList(stockQuotesDiff: List<StockQuoteDayComparisonDto>): List<StockReportItem> =
        stockQuotesDiff.map {
            val percentChange =
                if (it.todayQuote != null &&
                    it.yesterdayQuote != null &&
                    it.yesterdayQuote.compareTo(BigDecimal.ZERO) != 0
                ) {
                    (
                        (it.todayQuote - it.yesterdayQuote) / it.yesterdayQuote *
                            BigDecimal(
                                100,
                            )
                    ).setScale(2, java.math.RoundingMode.HALF_UP)
                } else {
                    BigDecimal.ZERO
                }
            val changeClass =
                when {
                    percentChange > BigDecimal.ZERO -> "quote-up"
                    percentChange < BigDecimal.ZERO -> "quote-down"
                    else -> "quote-neutral"
                }

            StockReportItem(
                ticker = it.ticker,
                companyName = it.companyName,
                todayQuote = (it.todayQuote?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "-"),
                yesterdayQuote = (
                    it.yesterdayQuote
                        ?.setScale(
                            2,
                            java.math.RoundingMode.HALF_UP,
                        )?.toPlainString() ?: "-"
                ),
                percentChange = percentChange.toPlainString(),
                changeClass = changeClass,
            )
        }

    private fun buildReportContext(
        days: Int,
        stocksList: List<StockReportItem>,
    ): Map<String, Any> {
        val highestGain = stocksList.maxByOrNull { it.percentChange.toBigDecimalOrNull() ?: BigDecimal.ZERO }
        val highestLoss = stocksList.minByOrNull { it.percentChange.toBigDecimalOrNull() ?: BigDecimal.ZERO }
        val highestToday = stocksList.maxByOrNull { it.todayQuote.toBigDecimalOrNull() ?: BigDecimal.ZERO }
        val lowestToday = stocksList.minByOrNull { it.todayQuote.toBigDecimalOrNull() ?: BigDecimal.ZERO }

        return mapOf(
            "days" to days,
            "stocks" to stocksList,
            "highestGain" to (highestGain ?: StockReportItem("-", "-", "-", "-", "-", "quote-neutral")),
            "highestLoss" to (highestLoss ?: StockReportItem("-", "-", "-", "-", "-", "quote-neutral")),
            "highestToday" to (highestToday ?: StockReportItem("-", "-", "-", "-", "-", "quote-neutral")),
            "lowestToday" to (lowestToday ?: StockReportItem("-", "-", "-", "-", "-", "quote-neutral")),
        )
    }

    private fun getStockQuotesToday(stockIds: List<UUID>): List<StockQuoteEntity> {
        val today = LocalDate.now()
        return this.getStockQuotesByDate(stockIds, today)
    }

    private fun getStockQuotesYesterday(stockIds: List<UUID>): List<StockQuoteEntity> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        return this.getStockQuotesByDate(stockIds, yesterday)
    }

    private fun getStockQuotesByDate(
        stockIds: List<UUID>,
        date: LocalDate,
    ): List<StockQuoteEntity> = stockQuoteRepository.findByStockEntityIdInAndQuoteDate(stockIds, date)

    private fun getStockQuotesDiff(
        stocks: List<StockEntity>,
        quotesToday: List<StockQuoteEntity>,
        quotesYesterday: List<StockQuoteEntity>,
    ): List<StockQuoteDayComparisonDto> =
        stocks.map { stock ->
            val todayQuote = quotesToday.find { it.stockEntity?.id == stock.id }?.closePrice
            val yesterdayQuote = quotesYesterday.find { it.stockEntity?.id == stock.id }?.closePrice

            StockQuoteDayComparisonDto(
                ticker = stock.ticker,
                companyName = stock.companyName,
                todayQuote = todayQuote,
                yesterdayQuote = yesterdayQuote,
            )
        }

    private fun getDateNow(): LocalDate = LocalDate.now()

    private fun save(stockQuoteEntity: StockQuoteEntity) {
        stockQuoteRepository.save(stockQuoteEntity)
    }

    private fun buildStockQuote(
        stockEntity: StockEntity,
        quoteResponse: StockQuoteQueryResponse,
    ): StockQuoteEntity =
        StockQuoteEntity(
            stockEntity = stockEntity,
            quoteDate = this.getDateNow(),
            openPrice = quoteResponse.openPrice,
            highPrice = quoteResponse.highPrice,
            lowPrice = quoteResponse.lowPrice,
            closePrice = quoteResponse.closePrice,
            volume = quoteResponse.volume,
        )

    private fun getStockPendingQuoteForToday(): StockEntity? {
        val today = this.getDateNow()
        val stocks = stockRepository.findAll()
        val stockQuotes = this.getStockQuotesByDateAndStockIds(date = today, stockEntities = stocks)

        return stocks.firstOrNull { stock ->
            stockQuotes
                .none { quote -> quote.stockEntity!!.id == stock.id }
        }
    }

    private fun getStockQuotesByDateAndStockIds(
        date: LocalDate,
        stockEntities: List<StockEntity>,
    ): List<StockQuoteEntity> {
        val stockIds = stockEntities.mapNotNull { it.id }
        return stockQuoteRepository.findByStockEntityIdInAndQuoteDate(stockIds, date)
    }

    private fun getQuoteFromProvider(stockEntity: StockEntity): StockQuoteQueryResponse? =
        stockQuoteProvider
            .getQuote(stockEntity.ticker)
            .orElse(null)
}
