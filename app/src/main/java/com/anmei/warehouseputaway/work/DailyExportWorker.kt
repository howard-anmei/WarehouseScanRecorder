package com.anmei.warehouseputaway.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anmei.warehouseputaway.data.ScanExporter
import com.anmei.warehouseputaway.data.local.database.ScanRecordDao
import com.anmei.warehouseputaway.data.preferences.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class DailyExportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scanRecordDao: ScanRecordDao,
    private val userPreferences: UserPreferences
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {

        println(
            "DailyExportWorker: ========== START =========="
        )

        return try {

            // ========================================
            // 1. 获取今天 00:00:00
            // ========================================

            val todayStartCalendar =
                Calendar.getInstance().apply {

                    set(
                        Calendar.HOUR_OF_DAY,
                        0
                    )

                    set(
                        Calendar.MINUTE,
                        0
                    )

                    set(
                        Calendar.SECOND,
                        0
                    )

                    set(
                        Calendar.MILLISECOND,
                        0
                    )
                }

            val todayStart =
                todayStartCalendar.timeInMillis

            // ========================================
            // 2. 获取当前 Worker 执行时间
            //
            // Worker 会在 22:00 左右执行。
            // WorkManager 可能存在少量延迟。
            // ========================================

            val exportEnd =
                System.currentTimeMillis()

            println(
                "DailyExportWorker: " +
                        "Export range: " +
                        "$todayStart -> $exportEnd"
            )

            // ========================================
            // 3. 获取今天的扫描记录
            // ========================================

            val records =
                scanRecordDao.getRecordsBetween(
                    startTime = todayStart,
                    endTime = exportEnd
                )

            println(
                "DailyExportWorker: " +
                        "records=${records.size}"
            )

            // ========================================
            // 4. 没有扫描记录
            // ========================================

            if (records.isEmpty()) {

                println(
                    "DailyExportWorker: " +
                            "No scan records to export"
                )

                cleanupOldCsvFiles()

                // 安排下一次 22:00
                DailyExportScheduler.scheduleNext(
                    applicationContext
                )

                println(
                    "DailyExportWorker: " +
                            "========== SUCCESS =========="
                )

                return Result.success()
            }

            // ========================================
            // 5. 获取操作员姓名
            // ========================================

            val operatorName =
                userPreferences.getOperatorName()

            println(
                "DailyExportWorker: " +
                        "operator=$operatorName"
            )

            // ========================================
            // 6. 导出 CSV
            //
            // 使用 data.ScanExporter
            //
            // CSV：
            // 序号,条码,扫描时间
            //
            // 不包含 Barcode Type
            // ========================================

            val fileName =
                ScanExporter.export(
                    context = applicationContext,
                    records = records,
                    operatorName = operatorName
                )

            println(
                "DailyExportWorker: " +
                        "file=$fileName"
            )

            // ========================================
            // 7. 确认导出结果
            // ========================================

            if (fileName.isBlank()) {

                println(
                    "DailyExportWorker: " +
                            "Export returned blank filename"
                )

                return Result.retry()
            }

            // ========================================
            // 8. 获取本次导出的记录 ID
            // ========================================

            val exportedIds =
                records.map { record ->
                    record.id
                }

            println(
                "DailyExportWorker: " +
                        "exportedIds=${exportedIds.size}"
            )

            // ========================================
            // 9. 删除本次已经成功导出的记录
            //
            // 重要：
            // 只删除 records 中对应的 ID。
            //
            // 不使用时间范围删除，
            // 避免误删导出过程中刚刚产生的新扫描记录。
            // ========================================

            val deletedCount =
                scanRecordDao.deleteByIds(
                    exportedIds
                )

            println(
                "DailyExportWorker: " +
                        "deleted=$deletedCount"
            )

            // ========================================
            // 10. 清理 30 天以前的 CSV
            //
            // 清理失败不会影响本次导出。
            // ========================================

            cleanupOldCsvFiles()

            // ========================================
            // 11. 安排下一次 22:00
            // ========================================

            DailyExportScheduler.scheduleNext(
                applicationContext
            )

            // ========================================
            // 12. 完成
            // ========================================

            println(
                "DailyExportWorker: " +
                        "Export successful"
            )

            println(
                "DailyExportWorker: " +
                        "file=$fileName"
            )

            println(
                "DailyExportWorker: " +
                        "records=${records.size}"
            )

            println(
                "DailyExportWorker: " +
                        "deleted=$deletedCount"
            )

            println(
                "DailyExportWorker: " +
                        "========== SUCCESS =========="
            )

            Result.success()

        } catch (e: Exception) {

            e.printStackTrace()

            println(
                "DailyExportWorker: " +
                        "========== FAILED =========="
            )

            println(
                "DailyExportWorker: " +
                        "error=${e.message}"
            )

            /*
             * 导出失败：
             *
             * 1. 不删除数据库记录
             * 2. 返回 retry
             * 3. WorkManager 可以再次执行
             */
            Result.retry()
        }
    }

    /**
     * 清理超过 30 天的旧 CSV。
     *
     * 清理失败不会导致本次导出失败。
     */
    private suspend fun cleanupOldCsvFiles() {

        try {

            val deletedCount =
                ScanExporter.deleteOldExports(
                    context = applicationContext,
                    retentionDays = 30
                )

            println(
                "DailyExportWorker: " +
                        "Old CSV cleanup: " +
                        "deleted=$deletedCount"
            )

        } catch (e: Exception) {

            e.printStackTrace()

            println(
                "DailyExportWorker: " +
                        "Old CSV cleanup failed: " +
                        e.message
            )
        }
    }
}