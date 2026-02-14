package com.nazarethlabs.joseph.stock

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Data Transfer Object para criar uma nova ação.")
data class CreateStockRequest(
    @get:Schema(
        description = "O ticker (código) da ação.",
        example = "PETR4",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val ticker: String,
    @get:Schema(description = "O nome da empresa.", example = "Petrobras", requiredMode = Schema.RequiredMode.REQUIRED)
    val companyName: String,
)

@Schema(description = "Data Transfer Object para representar uma ação existente.")
data class StockResponse(
    @get:Schema(description = "Identificador único da ação no banco.", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: UUID,
    @get:Schema(description = "O ticker (código) da ação.", example = "PETR4")
    val ticker: String,
    @get:Schema(description = "O nome da empresa.", example = "Petrobras")
    val companyName: String,
)
