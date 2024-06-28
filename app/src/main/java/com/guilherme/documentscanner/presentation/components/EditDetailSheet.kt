package com.guilherme.documentscanner.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.guilherme.documentscanner.R
import com.guilherme.documentscanner.presentation.MainViewModelEvents
import com.guilherme.documentscanner.presentation.MainViewState

@Composable
fun EditDetailSheet(
    state: MainViewState,
    onEvent: (MainViewModelEvents) -> Unit
) {
    if (state.isEditDialogOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(MainViewModelEvents.DismissEditDialog) },
            dismissButton = {
                TextButton(onClick = { onEvent(MainViewModelEvents.DismissEditDialog) }) {
                    Text(text = stringResource(R.string.cancel_dialog_dismiss_button))
                }
            },
            confirmButton = {
                TextButton(onClick = { onEvent(MainViewModelEvents.SaveChanges) }) {
                    Text(text = stringResource(R.string.save_changes_dialog))
                }
            },
            icon = {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            },
            title = {
                Text(text = stringResource(R.string.edit_dialog_title))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.document_name_dialog))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.documentName ?: "",
                        maxLines = 1,
                        onValueChange = {
                            onEvent(MainViewModelEvents.OnNameChanged(it))
                        },
                    )
                }
            }
        )
    }
}