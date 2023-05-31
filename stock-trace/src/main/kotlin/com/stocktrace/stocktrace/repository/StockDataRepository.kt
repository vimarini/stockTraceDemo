package com.stocktrace.stocktrace.repository

import com.stocktrace.stocktrace.domain.StockData
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface StockDataRepository : ReactiveCrudRepository<StockData,Int>{
    fun findByName(name: String): Flux<StockData>
}