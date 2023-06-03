package com.stocktrace.stocktrace.repository

import com.stocktrace.stocktrace.domain.StockData
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface StockDataRepository : ReactiveCrudRepository<StockData,Int>{
    fun findByNameIgnoreCase(name: String): Flux<StockData>
}