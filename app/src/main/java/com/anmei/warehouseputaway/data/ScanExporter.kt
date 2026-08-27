package com.anmei.warehouseputaway.data

import android.content.ContentValues
import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.anmei.warehouseputaway.data.local.database.ScanRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScanExporter {

    private const val FOLDER_NAME =
        "WarehousePutaway"

    /**
     * 导出扫描记录。
     *
     * 文件：
     *
     * Download/WarehousePutaway/
     *
     * 文件名：
     *
     * scan_records_OperatorName_yyyyMMdd_HHmmss.csv
     *
     * CSV 内容：
     *
     * 序号,条码,扫描时间
     *
     * 不再导出 Barcode Type。
     */
    fun export(
        context: Context,
        records: List<ScanRecord>,
        operatorName: String
    ): String {

        require(records.isNotEmpty()) {
            "没有扫描记录可以导出"
        }

        val safeOperatorName =
            operatorName
                .trim()
                .replace(
                    Regex("[\\\\/:*?\"<>|]"),
                    "_"
                )
                .ifEmpty {
                    "Unknown"
                }

        val fileName =
            "scan_records_${safeOperatorName}_${
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(Date())
            }.csv"

        val csvContent =
            buildCsv(records)

        val resolver =
            context.contentResolver

        val values =
            ContentValues().apply {

                put(
                    MediaStore.Downloads.DISPLAY_NAME,
                    fileName
                )

                put(
                    MediaStore.Downloads.MIME_TYPE,
                    "text/csv"
                )

                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME"
                )
            }

        val uri =
            resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            )
                ?: throw IllegalStateException(
                    "无法创建导出文件"
                )

        try {

            resolver
                .openOutputStream(uri)
                ?.use { outputStream ->

                    /*
                     * UTF-8 BOM
                     *
                     * 方便 Excel 正确识别中文。
                     */
                    outputStream.write(
                        byteArrayOf(
                            0xEF.toByte(),
                            0xBB.toByte(),
                            0xBF.toByte()
                        )
                    )

                    outputStream.write(
                        csvContent.toByteArray(
                            Charsets.UTF_8
                        )
                    )
                }
                ?: throw IllegalStateException(
                    "无法打开导出文件"
                )

        } catch (e: Exception) {

            resolver.delete(
                uri,
                null,
                null
            )

            throw e
        }

        return fileName
    }

    /**
     * 构建 CSV。
     *
     * 只有：
     *
     * 序号
     * 条码
     * 扫描时间
     */
    private fun buildCsv(
        records: List<ScanRecord>
    ): String {

        val builder =
            StringBuilder()

        builder.append(
            "序号,条码,扫描时间\n"
        )

        val dateFormat =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            )

        records.forEachIndexed { index, record ->

            val time =
                dateFormat.format(
                    Date(record.scanTime)
                )

            builder.append(
                index + 1
            )

            builder.append(",")

            builder.append(
                csvEscape(record.barcode)
            )

            builder.append(",")

            builder.append(
                csvEscape(time)
            )

            builder.append("\n")
        }

        return builder.toString()
    }

    /**
     * CSV 字段转义。
     */
    private fun csvEscape(
        value: String
    ): String {

        val escaped =
            value.replace(
                "\"",
                "\"\""
            )

        return "\"$escaped\""
    }

    /**
     * 删除超过指定天数的旧 CSV 文件。
     *
     * 清理失败由 Worker 自己处理。
     */
    fun deleteOldExports(
        context: Context,
        retentionDays: Int
    ): Int {

        val resolver =
            context.contentResolver

        val cutoff =
            System.currentTimeMillis() -
                    retentionDays *
                    24L *
                    60L *
                    60L *
                    1000L

        val collection =
            MediaStore.Downloads.EXTERNAL_CONTENT_URI

        val projection =
            arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.DATE_MODIFIED
            )

        val selection =
            "${MediaStore.Downloads.RELATIVE_PATH} = ?"

        val selectionArgs =
            arrayOf(
                "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME/"
            )

        var deletedCount = 0

        resolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->

            val idIndex =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Downloads._ID
                )

            val nameIndex =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Downloads.DISPLAY_NAME
                )

            val dateIndex =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Downloads.DATE_MODIFIED
                )

            while (cursor.moveToNext()) {

                val name =
                    cursor.getString(nameIndex)

                /*
                 * 只处理我们的 CSV。
                 */
                if (
                    !name.startsWith("scan_records_") ||
                    !name.endsWith(".csv")
                ) {
                    continue
                }

                val modifiedSeconds =
                    cursor.getLong(dateIndex)

                val modifiedMillis =
                    modifiedSeconds * 1000L

                if (modifiedMillis < cutoff) {

                    val id =
                        cursor.getLong(idIndex)

                    val uri =
                        ContentUris.withAppendedId(
                            collection,
                            id
                        )

                    if (
                        resolver.delete(
                            uri,
                            null,
                            null
                        ) > 0
                    ) {
                        deletedCount++
                    }
                }
            }
        }

        return deletedCount
    }
}