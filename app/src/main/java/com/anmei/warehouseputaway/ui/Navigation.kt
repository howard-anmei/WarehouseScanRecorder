package com.anmei.warehouseputaway.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.anmei.warehouseputaway.ui.scanner.ScannerScreen
import com.anmei.warehouseputaway.ui.scanner.ScannerViewModel

@Composable
fun MainNavigation() {

    val viewModel: ScannerViewModel =
        hiltViewModel()

    ScannerScreen(
        viewModel = viewModel
    )
}