package com.anmei.warehouseputaway.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val barcode: String,

    val barcodeType: String,

    val scanTime: Long
)