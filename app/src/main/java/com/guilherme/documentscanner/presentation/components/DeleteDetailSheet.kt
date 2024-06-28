package com.guilherme.documentscanner.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.guilherme.documentscanner.R
import com.guilherme.documentscanner.presentation.MainViewModelEvents
import com.guilherme.documentscanner.presentation.MainViewState

@Composable
fun DeleteDetailSheet(
    state: MainViewState,
    onEvent: (MainViewModelEvents) -> Unit
) {
    if (state.isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                onEvent(MainViewModelEvents.DismissDeleteDialog)
            },
            dismissButton = {
                TextButton(onClick = { onEvent(MainViewModelEvents.DismissDeleteDialog) }) {
                    Text(
                        text = stringResource(R.string.dialog_cancel_delete_btn),
                    )
                }
            },
            confirmButton = {

                val snackBarMessage = stringResource(R.string.snackbar_delete_error)
                val snackBarLabel = stringResource(id = R.string.close)

                TextButton(onClick = {
                    onEvent(
                        MainViewModelEvents.DeleteDocument(
                            value = state.selectedDocument!!,
                            message = snackBarMessage,
                            label = snackBarLabel
                        )
                    )
                }) {
                    Text(
                        text = stringResource(R.string.dialog_confirm_delete_btn),
                        color = Color.Red
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.dialog_title_delete),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_icon_desc),
                    tint = Color.Red
                )
            },
            text = {
                Text(text = stringResource(R.string.dialog_confirm_delete_message))
            }
        )
    }
}