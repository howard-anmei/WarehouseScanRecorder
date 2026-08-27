package com.anmei.warehouseputaway.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.anmei.warehouseputaway.data.ScanRecordRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScanReceiver : BroadcastReceiver() {

    companion object {

        private const val TAG = "ScanReceiver"

        private const val ACTION_DECODE_DATA =
            "android.intent.ACTION_DECODE_DATA"

        private const val EXTRA_BARCODE_STRING =
            "barcode_string"

        private const val EXTRA_SYMBOLOGY_NAME =
            "com.ubx.datawedge.symbology_name"
    }

    @Inject
    lateinit var repository: ScanRecordRepository

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        Log.d(
            TAG,
            "========== SCAN EVENT =========="
        )

        Log.d(
            TAG,
            "Action: ${intent.action}"
        )

        if (intent.action != ACTION_DECODE_DATA) {

            Log.d(
                TAG,
                "Ignore unknown action"
            )

            return
        }

        val barcode =
            intent.getStringExtra(
                EXTRA_BARCODE_STRING
            )

        val barcodeType =
            intent.getStringExtra(
                EXTRA_SYMBOLOGY_NAME
            )
                ?: intent.getStringExtra(
                    "symName"
                )
                ?: intent.getStringExtra(
                    "codetype"
                )
                ?: "Unknown"

        Log.d(
            TAG,
            "Barcode = $barcode"
        )

        Log.d(
            TAG,
            "BarcodeType = $barcodeType"
        )

        if (barcode.isNullOrBlank()) {

            Log.d(
                TAG,
                "Barcode is empty"
            )

            return
        }

        /*
         * BroadcastReceiver 生命周期很短。
         *
         * goAsync() 允许我们在 Receiver 返回后，
         * 给异步数据库操作一点时间完成。
         */
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                // ========================================
                // 1. Save scan record to Room
                // ========================================

                repository.add(
                    barcode = barcode,
                    barcodeType = barcodeType
                )

                Log.d(
                    TAG,
                    "Scan record saved to database"
                )

                // ========================================
                // 2. Notify UI
                // ========================================

                ScannerManager.emit(
                    ScanEvent(
                        barcode = barcode,
                        barcodeType = barcodeType
                    )
                )

                Log.d(
                    TAG,
                    "ScanEvent emitted"

                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Failed to save scan record",
                    e
                )

            } finally {

                pendingResult.finish()
            }
        }

        Log.d(
            TAG,
            "================================"
        )
    }
}