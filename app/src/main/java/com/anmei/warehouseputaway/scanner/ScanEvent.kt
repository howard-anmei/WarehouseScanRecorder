package com.anmei.warehouseputaway.scanner

data class ScanEvent(
    val barcode: String,
    val barcodeType: String = "Unknown",
    val scanTime: Long = System.currentTimeMillis()
)