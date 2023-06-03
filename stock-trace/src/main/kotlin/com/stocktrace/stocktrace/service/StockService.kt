package com.stocktrace.stocktrace.service

import com.google.gson.Gson
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
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

    fun captureData(stock: Stock) {
        var count = 0;
        Flux.interval(Duration.ofSeconds(20))
            .flatMap { Flux.just(webClientGetStock(stock.stockName)) }
            .takeWhile { value -> value != NOTFOUND || count>=40}
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
        var price = NOTFOUND
        val client = HttpClient.newHttpClient()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:5000/${stock}"))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        val statusCode = response.statusCode()
        val responseBody = response.body()
        val gson = Gson()
        val stockResponseFromPython = gson.fromJson(responseBody, StockResponseFromPython::class.java)
        if (statusCode==200) {
            price = stockResponseFromPython.price.replace(",",".").toFloat()
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