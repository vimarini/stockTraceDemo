package com.stocktrace.stocktrace

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StockTraceApplication

fun main(args: Array<String>) {
	runApplication<StockTraceApplication>(*args)
}
