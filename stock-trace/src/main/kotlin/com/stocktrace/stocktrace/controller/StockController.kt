package com.stocktrace.stocktrace.controller

import com.stocktrace.stocktrace.domain.Stock
import com.stocktrace.stocktrace.domain.StockData
import com.stocktrace.stocktrace.service.StockService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import javax.validation.Valid


@RestController
@RequestMapping("stock")
class StockController(private val stockService: StockService) {

//    @PostMapping("/request")
//    @CrossOrigin
//    @ResponseStatus(HttpStatus.CREATED)
//    fun startSearch(@RequestBody stock: Stock){
//        return stockService.captureData(stock)
//    }

    @PostMapping("/request")
    @CrossOrigin
    @ResponseStatus(HttpStatus.CREATED)
    fun startSearch(@RequestBody stock: Stock){
        return stockService.captureDataLocal(stock)
    }

    @PostMapping
    @CrossOrigin
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@Valid @RequestBody stockData: StockData) : Mono<StockData> {
        return stockService.save(stockData)
    }

//    @GetMapping(path = ["{stock}"])
//    @CrossOrigin
//    fun getStock(@PathVariable stock : String) : Mono<StockData> {
//        return stockService.findByName(stock)
//    }

    @GetMapping(path = ["{stock}"])
    @CrossOrigin
    fun getStock(@PathVariable stock : String) : Mono<StockData> {
        return stockService.findByNameLocal(stock)
    }
}