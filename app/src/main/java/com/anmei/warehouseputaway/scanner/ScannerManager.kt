package com.anmei.warehouseputaway.scanner

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ScannerManager {

    private val _scanEvents =
        MutableSharedFlow<ScanEvent>(
            replay = 1,
            extraBufferCapacity = 10
        )

    val scanEvents: SharedFlow<ScanEvent> =
        _scanEvents.asSharedFlow()

    fun emit(event: ScanEvent) {
        _scanEvents.tryEmit(event)
    }
}