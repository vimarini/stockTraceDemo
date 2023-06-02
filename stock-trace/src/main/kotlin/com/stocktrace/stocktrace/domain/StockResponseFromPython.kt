package com.stocktrace.stocktrace.domain

data class StockResponseFromPython(
    val stock: String,
    val price: String,
    val time: String
)