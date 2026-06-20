package me.weishu.kernelsu.ui.component.choosekmidialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.material.SegmentedColumn
import me.weishu.kernelsu.ui.component.material.SegmentedRadioItem
import me.weishu.kernelsu.ui.util.getCurrentKmi
import me.weishu.kernelsu.ui.util.getSupportedKmis

@Composable
fun ChooseKmiDialogMaterial(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (String?) -> Unit
) {
    if (!show) return

    val supportedKMIs by produceState(initialValue = emptyList()) {
        value = getSupportedKmis()
    }

    val currentKmi by produceState(initialValue = "") {
        value = getCurrentKmi()
    }

    val selectedKmi = remember(currentKmi) { mutableStateOf(currentKmi) }

    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
            selectedKmi.value = currentKmi
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSelected(selectedKmi.value)
                    onDismissRequest()
                },
                enabled = supportedKMIs.contains(selectedKmi.value)
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
                selectedKmi.value = currentKmi
            }) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            // Thay vì chỉ hiện 1 dòng Text, tui dùng Column để chứa Title và Subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_kmi),
                    textAlign = TextAlign.Center
                )
                
                // Nếu lấy được KMI của máy thì hiện dòng phụ đề giống hình ông gửi
                if (currentKmi.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Phiên bản KMI của thiết bị này: $currentKmi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        text = {
            SegmentedColumn(
                content = supportedKMIs.map { kmi ->
                    {
                        SegmentedRadioItem(
                            title = kmi,
                            summary = if (kmi == currentKmi) stringResource(R.string.current_device_kmi) else null,
                            selected = selectedKmi.value == kmi,
                            onClick = { selectedKmi.value = kmi }
                        )
                    }
                }
            )
        }
    )
}