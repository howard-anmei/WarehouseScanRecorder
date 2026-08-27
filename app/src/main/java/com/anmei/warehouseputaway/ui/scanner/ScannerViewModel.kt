package com.anmei.warehouseputaway.ui.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anmei.warehouseputaway.data.ScanExporter
import com.anmei.warehouseputaway.data.ScanRecordRepository
import com.anmei.warehouseputaway.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    application: Application,
    private val repository: ScanRecordRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    /**
     * 最近一条扫描记录
     */
    val latestRecord =
        repository.latestRecord

    /**
     * 当前数据库中的扫描数量
     */
    val scanCount =
        repository.count

    /**
     * 所有扫描记录
     */
    val records =
        repository.records

    /**
     * 操作员姓名输入框内容
     */
    private val _operatorNameInput =
        MutableStateFlow("")

    val operatorNameInput: StateFlow<String> =
        _operatorNameInput.asStateFlow()

    /**
     * 导出状态消息
     */
    private val _exportMessage =
        MutableStateFlow<String?>(null)

    val exportMessage: StateFlow<String?> =
        _exportMessage.asStateFlow()

    init {

        /*
         * 从本地保存的 Preferences
         * 读取操作员姓名。
         *
         * getOperatorName() 是 suspend，
         * 所以必须在协程中调用。
         */
        viewModelScope.launch {

            try {

                val name =
                    userPreferences.getOperatorName()

                _operatorNameInput.value =
                    name

            } catch (e: Exception) {

                _operatorNameInput.value =
                    ""
            }
        }
    }

    /**
     * 更新操作员姓名输入框。
     */
    fun updateOperatorName(
        name: String
    ) {

        _operatorNameInput.value =
            name
    }

    /**
     * 保存当前操作员姓名。
     */
    fun saveOperatorName() {

        viewModelScope.launch {

            try {

                userPreferences.saveOperatorName(
                    _operatorNameInput.value.trim()
                )

                _exportMessage.value =
                    "操作员姓名已保存"

            } catch (e: Exception) {

                _exportMessage.value =
                    "保存失败：${e.message ?: "未知错误"}"
            }
        }
    }

    /**
     * 手动导出全部当前扫描记录。
     *
     * 流程：
     *
     * 1. 获取当前全部记录
     * 2. 导出 CSV
     * 3. CSV 成功创建后
     * 4. 删除本次已经导出的记录
     *
     * 如果导出失败：
     *
     * 不删除数据库记录。
     */
    fun exportRecords() {

        viewModelScope.launch {

            try {

                _exportMessage.value =
                    "正在导出..."

                /*
                 * 获取当前全部记录。
                 */
                val recordsToExport =
                    repository.getAllRecords()

                if (recordsToExport.isEmpty()) {

                    _exportMessage.value =
                        "没有扫描记录可以导出"

                    return@launch
                }

                /*
                 * 获取当前操作员姓名。
                 */
                val operatorName =
                    userPreferences
                        .getOperatorName()
                        .trim()

                /*
                 * 如果 Preferences 中没有，
                 * 使用输入框当前内容。
                 */
                val finalOperatorName =
                    if (operatorName.isNotEmpty()) {
                        operatorName
                    } else {
                        _operatorNameInput.value
                            .trim()
                    }

                /*
                 * 如果仍然为空，
                 * 使用 Unknown。
                 *
                 * ScanExporter 本身也会处理，
                 * 这里保持逻辑明确。
                 */
                val safeOperatorName =
                    finalOperatorName
                        .ifEmpty {
                            "Unknown"
                        }

                /*
                 * 保存本次导出的数量。
                 */
                val recordCount =
                    recordsToExport.size

                /*
                 * CSV 导出。
                 *
                 * 注意：
                 * ScanExporter 是 object，
                 * 所以直接调用 ScanExporter.export()。
                 */
                val fileName =
                    ScanExporter.export(
                        context =
                            getApplication<Application>(),
                        records =
                            recordsToExport,
                        operatorName =
                            safeOperatorName
                    )

                /*
                 * CSV 已经成功创建。
                 *
                 * 现在只删除刚刚导出的这些记录。
                 */
                repository.deleteRecords(
                    recordsToExport
                )

                /*
                 * 导出成功。
                 */
                _exportMessage.value =
                    "导出成功：$fileName\n已删除 $recordCount 条记录"

            } catch (e: Exception) {

                e.printStackTrace()

                /*
                 * 导出失败时：
                 *
                 * 不删除数据库记录。
                 */
                _exportMessage.value =
                    "导出失败：${e.message ?: "未知错误"}"
            }
        }
    }

    /**
     * 清除导出提示。
     */
    fun clearExportMessage() {

        _exportMessage.value =
            null
    }
}