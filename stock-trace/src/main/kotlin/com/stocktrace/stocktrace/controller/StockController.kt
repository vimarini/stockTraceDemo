package com.stocktrace.stocktrace.controller

import com.stocktrace.stocktrace.domain.Stock
import com.stocktrace.stocktrace.domain.StockData
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import com.stocktrace.stocktrace.service.StockService
import reactor.core.publisher.Flux
import javax.validation.Valid

@RestController
@RequestMapping("stock")
class StockController(private val stockService: StockService) {

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendRequestSelenium(@RequestBody stock: Stock){
        return stockService.captureData(stock)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@Valid @RequestBody stockData: StockData) : Mono<StockData> {
        return stockService.save(stockData)
    }

    @GetMapping(path = ["{stock}"])
    fun getStock(@PathVariable stock : String) : Flux<StockData> {
        return stockService.findByName(stock)
    }
}