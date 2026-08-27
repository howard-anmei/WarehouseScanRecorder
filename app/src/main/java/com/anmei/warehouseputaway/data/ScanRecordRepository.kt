package com.anmei.warehouseputaway.data

import com.anmei.warehouseputaway.data.local.database.ScanRecord
import com.anmei.warehouseputaway.data.local.database.ScanRecordDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRecordRepository @Inject constructor(
    private val dao: ScanRecordDao
) {

    /**
     * 所有扫描记录
     */
    val records: Flow<List<ScanRecord>> =
        dao.observeAll()

    /**
     * 扫描记录数量
     */
    val count: Flow<Int> =
        dao.observeCount()

    /**
     * 最近一条扫描记录
     */
    val latestRecord: Flow<ScanRecord?> =
        dao.observeLatest()

    /**
     * 保存扫描记录。
     *
     * barcodeType 继续保存到数据库，
     * 但新的 CSV 不再导出 Barcode Type。
     */
    suspend fun add(
        barcode: String,
        barcodeType: String,
        scanTime: Long = System.currentTimeMillis()
    ) {

        dao.insert(
            ScanRecord(
                barcode = barcode,
                barcodeType = barcodeType,
                scanTime = scanTime
            )
        )
    }

    /**
     * 获取全部扫描记录。
     */
    suspend fun getAllRecords(): List<ScanRecord> {

        return dao.getAllRecords()
    }

    /**
     * 删除指定 ID 的扫描记录。
     *
     * 只删除本次已经成功导出的记录。
     */
    suspend fun deleteByIds(
        ids: List<Long>
    ): Int {

        if (ids.isEmpty()) {
            return 0
        }

        return dao.deleteByIds(ids)
    }

    /**
     * 删除指定的扫描记录。
     *
     * 这是为了兼容 ScannerViewModel。
     *
     * 实际删除时使用记录的 ID，
     * 不使用时间范围，避免误删新扫描的数据。
     */
    suspend fun deleteRecords(
        records: List<ScanRecord>
    ): Int {

        if (records.isEmpty()) {
            return 0
        }

        val ids =
            records.map {
                it.id
            }

        return dao.deleteByIds(ids)
    }

    /**
     * 获取指定时间范围内的扫描记录。
     */
    suspend fun getRecordsBetween(
        startTime: Long,
        endTime: Long
    ): List<ScanRecord> {

        return dao.getRecordsBetween(
            startTime = startTime,
            endTime = endTime
        )
    }

    /**
     * 删除指定时间范围内的扫描记录。
     */
    suspend fun deleteRecordsBetween(
        startTime: Long,
        endTime: Long
    ): Int {

        return dao.deleteRecordsBetween(
            startTime = startTime,
            endTime = endTime
        )
    }
}