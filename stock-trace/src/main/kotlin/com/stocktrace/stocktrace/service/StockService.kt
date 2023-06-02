package com.stocktrace.stocktrace.service

import com.stocktrace.stocktrace.domain.Stock
import com.stocktrace.stocktrace.domain.StockData
import com.stocktrace.stocktrace.domain.StockResponseFromPython
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
import kotlin.NoSuchElementException

@Service
class StockService {

    @Autowired
    lateinit var stockDataRepository: StockDataRepository

    @Autowired
    private lateinit var webClient: WebClient

    private val NOTFOUND: Float = 3.14151617F

    fun captureData(stock: Stock) {
        Flux.interval(Duration.ofSeconds(20))
            .flatMap { Flux.just(webClientGetStock(stock.stockName)) }
            .takeWhile { value -> value != NOTFOUND }
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
            }
    }


    fun seleniumStock(stock: String): Float {
        val currentPath = File("").absolutePath

        //linux
        System.setProperty("webdriver.chrome.driver", "${currentPath}/usr/local/bin/chromedriver")

        //win
//        System.setProperty("webdriver.chrome.driver", "${currentPath}/chromedriver.exe")

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

        driver.close()
        return currentValue.replace(",", ".").toFloat()
    }

    fun webClientPostCall(stockData: StockData) {
        webClient.post()
            .uri("/stock")
            .body(BodyInserters.fromValue(stockData))
            .retrieve()
            .bodyToMono(String::class.java)
            .subscribe()
    }

    fun webClientGetStock(stock: String) : Float{
        val updateWebClient = webClient.mutate()
            .baseUrl("http://localhost:5000")
            .build()
        var price = NOTFOUND
        var response = updateWebClient.get()
                    .uri("/${stock}")
                    .exchangeToMono { res ->
                        when {
                            res.statusCode().is2xxSuccessful -> res.bodyToMono(StockResponseFromPython::class.java)
                            else -> null
                        }
                    }

        response.subscribe {res ->
            if(res is StockResponseFromPython) {
                price = res.price.toFloat()
            }
        }
        return price
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