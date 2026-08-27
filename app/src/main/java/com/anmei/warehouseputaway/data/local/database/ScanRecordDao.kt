package com.anmei.warehouseputaway.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanRecordDao {

    /**
     * 插入扫描记录
     */
    @Insert
    suspend fun insert(
        record: ScanRecord
    )

    /**
     * 所有扫描记录
     *
     * 最新记录在前
     */
    @Query(
        """
        SELECT *
        FROM scan_records
        ORDER BY scanTime DESC
        """
    )
    fun observeAll(): Flow<List<ScanRecord>>

    /**
     * 扫描记录数量
     */
    @Query(
        """
        SELECT COUNT(*)
        FROM scan_records
        """
    )
    fun observeCount(): Flow<Int>

    /**
     * 最近一条扫描记录
     */
    @Query(
        """
        SELECT *
        FROM scan_records
        ORDER BY scanTime DESC
        LIMIT 1
        """
    )
    fun observeLatest(): Flow<ScanRecord?>

    /**
     * 获取全部扫描记录
     */
    @Query(
        """
        SELECT *
        FROM scan_records
        ORDER BY scanTime ASC
        """
    )
    suspend fun getAllRecords(): List<ScanRecord>

    /**
     * 获取指定时间范围内的记录
     *
     * startTime 包含
     * endTime 不包含
     */
    @Query(
        """
        SELECT *
        FROM scan_records
        WHERE scanTime >= :startTime
          AND scanTime < :endTime
        ORDER BY scanTime ASC
        """
    )
    suspend fun getRecordsBetween(
        startTime: Long,
        endTime: Long
    ): List<ScanRecord>

    /**
     * 根据 ID 删除记录。
     *
     * 手动导出和自动导出成功后使用。
     *
     * 只删除实际已经导出的记录。
     */
    @Query(
        """
        DELETE FROM scan_records
        WHERE id IN (:ids)
        """
    )
    suspend fun deleteByIds(
        ids: List<Long>
    ): Int

    /**
     * 按时间范围删除记录。
     *
     * 保留给旧代码兼容。
     */
    @Query(
        """
        DELETE FROM scan_records
        WHERE scanTime >= :startTime
          AND scanTime < :endTime
        """
    )
    suspend fun deleteRecordsBetween(
        startTime: Long,
        endTime: Long
    ): Int
}