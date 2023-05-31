package com.stocktrace.stocktrace.domain

import org.jetbrains.annotations.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import javax.validation.constraints.NotEmpty

@Table("stock")
class StockData(
    @field:NotNull
    @field:NotEmpty(message = "Name can't be empty!")
    val name:String,
    val action: String,
    val price: String,
    val time: LocalDateTime = LocalDateTime.now()
)