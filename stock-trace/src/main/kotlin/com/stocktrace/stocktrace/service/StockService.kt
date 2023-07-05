package com.stocktrace.stocktrace.service

import com.google.gson.Gson
import com.stocktrace.stocktrace.domain.Stock
import com.stocktrace.stocktrace.domain.StockData
import com.stocktrace.stocktrace.domain.StockResponseFromPython
import com.stocktrace.stocktrace.repository.StockDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service
class StockService {

    @Autowired
    lateinit var stockDataRepository: StockDataRepository

    @Autowired
    private lateinit var webClient: WebClient

    private val NOTFOUND: Float = 3.14151617F

    var localStockHash = mutableListOf<StockData>();

    fun captureData(stock: Stock) {
        var count = 0
        var disposable: Disposable? = null
        disposable = Flux.interval(Duration.ofSeconds(20))
            .flatMap { Flux.just(webClientGetStock(stock.stockName)) }
            .takeWhile { value -> (value != NOTFOUND) }
            .subscribe { value ->
                if (value < stock.minValue.toFloat()) webClientPostCall(
                    StockData(
                        stock.stockName,
                        "BUY",
                        value.toString()
                    )
                )
                else if (value > stock.maxValue.toFloat()) webClientPostCall(
                    StockData(
                        stock.stockName,
                        "SELL",
                        value.toString()
                    )
                )
                else webClientPostCall(StockData(stock.stockName, "WAIT", value.toString()))
                count++
                if (count >= 10) {
                    disposable?.dispose()
                    return@subscribe
                }
            }
    }

    fun captureDataLocal(stock: Stock) {
        var count = 0
        var disposable: Disposable? = null
        disposable = Flux.interval(Duration.ofSeconds(20))
            .log()
            .flatMap { Flux.just(webClientGetStock(stock.stockName)) }
            .takeWhile { value -> (value != NOTFOUND) }
            .subscribe { value ->
                if (value < stock.minValue.toFloat()) {
                    localStockHash.add(
                        StockData(
                            stock.stockName,
                            "BUY",
                            value.toString()
                        )
                    )
                } else if (value > stock.maxValue.toFloat()) {
                    localStockHash.add(
                        StockData(
                            stock.stockName,
                            "SELL",
                            value.toString()
                        )
                    )
                } else localStockHash.add(
                    StockData(
                        stock.stockName,
                        "WAIT",
                        value.toString()
                    )
                )
                count++
                if (count >= 10) {
                    disposable?.dispose()
                    return@subscribe
                }
            }
    }

    fun webClientPostCall(stockData: StockData) {
        webClient.post()
            .uri("/stock")
            .body(BodyInserters.fromValue(stockData))
            .retrieve()
            .bodyToMono(String::class.java)
            .subscribe()
    }

    fun webClientGetStock(stock: String): Float {
        var price = NOTFOUND
        val client = HttpClient.newHttpClient()
        lateinit var request: HttpRequest
        try {
            request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/${stock}"))
                .build()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return price
        }

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        val statusCode = response.statusCode()
        val responseBody = response.body()
        val gson = Gson()
        val stockResponseFromPython = gson.fromJson(responseBody, StockResponseFromPython::class.java)
        if (statusCode == 200) {
            price = stockResponseFromPython.price.replace(",", ".").toFloat()
        }
        return price
    }

    fun save(stockData: StockData): Mono<StockData> {
        return stockDataRepository.save(stockData)
    }

    fun findByName(stockName: String): Mono<StockData> {
        return stockDataRepository.findByNameIgnoreCase(stockName)
            .log()
            .reduce { stock1, stock2 ->
                if (stock1.time.isAfter(stock2.time))
                    stock1
                else
                    stock2
            }
    }

    fun findByNameLocal(stockName: String): Mono<StockData> {
        return if (localStockHash.isNotEmpty() && localStockHash.any {it.name==stockName}) {
            Mono.just(
                localStockHash.filter { value -> value.name == stockName }
                    .reduce { stock1, stock2 ->
                        if (stock1.time.isAfter(stock2.time))
                            stock1
                        else
                            stock2
                    }
            )
        } else {
            Mono.empty()
        }
    }
}