package com.stocktrace.stocktrace.service

import com.stocktrace.stocktrace.domain.Stock
import com.stocktrace.stocktrace.domain.StockData
import com.stocktrace.stocktrace.repository.StockDataRepository
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.time.Duration
import java.util.*
import kotlin.NoSuchElementException

@Service
class StockService {

    @Autowired
    lateinit var stockDataRepository: StockDataRepository

    @Autowired
    private lateinit var webClient: WebClient

    private val NOTFOUND: Float = 3.14151617F

    fun captureData(stock: Stock) {
        Flux.interval(Duration.ofSeconds(30))
            .flatMap { Flux.just(seleniumStock(stock.stockName)) }
            .takeWhile { value -> value != NOTFOUND }
            .subscribe { value ->
                if (value < stock.minValue.toFloat()) webClientCall(
                    StockData(
                        stock.stockName,
                        "BUY",
                        value.toString()
                    )
                )
                else if (value > stock.maxValue.toFloat()) webClientCall(
                    StockData(
                        stock.stockName,
                        "SELL",
                        value.toString()
                    )
                )
                else webClientCall(StockData(stock.stockName, "WAIT", value.toString()))
            }

    }


    fun seleniumStock(stock: String): Float {
        val currentPath = File("").absolutePath

        System.setProperty("webdriver.chrome.driver", "${currentPath}/chromedriver.exe")

        val chromeOptions = ChromeOptions()
        chromeOptions.addArguments("--headless")
        chromeOptions.addArguments("--disable-gpu")

        val driver: WebDriver = ChromeDriver(chromeOptions)

        driver.get("https://www.google.com/search?q=${stock}")
        var currentValue = ""

        try {
            currentValue =
                driver.findElement(By.xpath("//*[@id=\"knowledge-finance-wholepage__entity-summary\"]/div[3]/g-card-section/div/g-card-section/div[2]/div[1]/span[1]/span/span[1]")).text
        } catch (exception: NoSuchElementException) {
            println("NotFound")
            return NOTFOUND
        }

        return currentValue.replace(",", ".").toFloat()
    }

    fun webClientCall(stockData: StockData) {
        webClient.post()
            .uri("/stock")
            .body(BodyInserters.fromValue(stockData))
            .retrieve()
            .bodyToMono(String::class.java)
            .subscribe()
    }

    fun save(stockData: StockData): Mono<StockData> {
        return stockDataRepository.save(stockData)
    }

    fun findByName(stockName: String): Mono<StockData> {
        return stockDataRepository.findByNameIgnoreCase(stockName)
            .reduce { stock1, stock2 ->
                if (stock1.time.isAfter(stock2.time))
                    stock1
                else
                    stock2
            }
    }
}