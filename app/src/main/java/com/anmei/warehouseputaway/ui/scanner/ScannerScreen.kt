package com.anmei.warehouseputaway.ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel()
) {

    val latestRecord by
    viewModel.latestRecord
        .collectAsStateWithLifecycle(
            initialValue = null
        )

    val scanCount by
    viewModel.scanCount
        .collectAsStateWithLifecycle(
            initialValue = 0
        )

    val operatorNameInput by
    viewModel.operatorNameInput
        .collectAsStateWithLifecycle()

    val exportMessage by
    viewModel.exportMessage
        .collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Warehouse Putaway"
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * Operator name
         */

        Text(
            text = "Operator Name"
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = operatorNameInput,
                onValueChange = {
                    viewModel.updateOperatorName(it)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = {
                    Text("Operator Name")
                }
            )

            Button(
                onClick = {
                    viewModel.saveOperatorName()
                }
            ) {
                Text("Save")
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        /*
         * Latest scan
         */

        Text(
            text = "Latest Scan"
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        if (latestRecord == null) {

            Text(
                text = "No scan records"
            )

        } else {

            Text(
                text = latestRecord!!.barcode
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text =
                    "Type: ${latestRecord!!.barcodeType}"
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * Scan count
         */

        Text(
            text = "Saved Scan Count: $scanCount"
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * Manual export
         */

        Button(
            onClick = {
                viewModel.exportRecords()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export CSV")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * Export status
         */

        exportMessage?.let { message ->

            Text(
                text = message
            )
        }
    }
}