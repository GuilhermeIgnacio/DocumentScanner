package com.guilherme.documentscanner.presentation.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.guilherme.documentscanner.R
import com.guilherme.documentscanner.presentation.MainViewModelEvents
import com.guilherme.documentscanner.presentation.MainViewState

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun DocumentDetailSheet(state: MainViewState, onEvent: (MainViewModelEvents) -> Unit) {
    AnimatedVisibility(visible = state.isDetailSheetOpen) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { state.snackbarHostState }
            ) { _ ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onEvent(MainViewModelEvents.DismissDetailSheet) }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.return_button_desc)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = state.selectedDocument?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Column {
                            IconButton(
                                onClick = { onEvent(MainViewModelEvents.OnMenuClick) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = stringResource(R.string.open_dropdown_menu_desc),
                                )
                            }

                            DropdownMenu(
                                modifier = Modifier.align(Alignment.End),
                                expanded = state.isDropdownMenuOpen,
                                onDismissRequest = { onEvent(MainViewModelEvents.DismissDropdownMenu) }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (state.selectedDocument?.name.isNullOrEmpty()) stringResource(
                                                R.string.set_name_dropdown_item
                                            ) else stringResource(R.string.edit_name_dropdown_item),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    onClick = {
                                        onEvent(MainViewModelEvents.OnEditClick)
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.edit_document_desc)
                                        )
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.delete_dropdown_item),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Red
                                        )
                                    },
                                    onClick = {
                                        onEvent(MainViewModelEvents.OnDeleteClick)
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete_document_icon_desc),
                                            tint = Color.Red
                                        )
                                    }
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.selectedDocument?.let {
                            items(it.uri) {

                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(2)),
                                    model = it,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth
                                )

                            }
                        }
                    }
                }
            }
        }

        DeleteDetailSheet(state = state, onEvent = onEvent)
        EditDetailSheet(state = state, onEvent = onEvent)

    }
}