package com.guilherme.documentscanner.presentation

import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings.Global.getString
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guilherme.documentscanner.R
import com.guilherme.documentscanner.di.Document
import com.guilherme.documentscanner.di.RealmRepository
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.FileOutputStream

data class MainViewState(
    val data: List<Document> = emptyList(),
    val uriList: List<Uri> = emptyList(),
    val selectedDocument: Document? = null,
    val isDetailSheetOpen: Boolean = false,
    val isDropdownMenuOpen: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val isDeleteDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val documentName: String? = null
)

sealed interface MainViewModelEvents {
    data class OnDocumentAdded(val value: List<Uri>?) : MainViewModelEvents
    data class OnDocumentClick(val value: Document) : MainViewModelEvents
    data object DismissDetailSheet : MainViewModelEvents
    data object OnMenuClick : MainViewModelEvents
    data object DismissDropdownMenu : MainViewModelEvents
    data object OnDeleteClick : MainViewModelEvents
    data object DismissDeleteDialog : MainViewModelEvents
    data class DeleteDocument(val value: Document, val message: String, val label: String) :
        MainViewModelEvents

    data object OnEditClick : MainViewModelEvents
    data object DismissEditDialog : MainViewModelEvents
    data class OnNameChanged(val value: String) : MainViewModelEvents
    data object SaveChanges : MainViewModelEvents
}

class MainViewModel(
    private val repository: RealmRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.fetchData().collect { documents ->
                _state.update {
                    it.copy(
                        data = documents
                    )
                }
            }
        }
    }

    fun onEvent(event: MainViewModelEvents) {
        when (event) {
            is MainViewModelEvents.OnDocumentAdded -> {
                viewModelScope.launch {

                    println(_state.value.data)

                    val list = event.value?.map { it.toString() }

                    if (list != null) {
                        repository.writeObject(list)
                    }


                }
            }

            is MainViewModelEvents.OnDocumentClick -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            selectedDocument = event.value,
                            isDetailSheetOpen = true
                        )
                    }
                }
            }

            MainViewModelEvents.DismissDetailSheet -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            selectedDocument = null,
                            isDetailSheetOpen = false
                        )
                    }
                }
            }

            MainViewModelEvents.OnMenuClick -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isDropdownMenuOpen = true
                        )
                    }
                }
            }

            MainViewModelEvents.DismissDropdownMenu -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isDropdownMenuOpen = false
                        )
                    }
                }
            }

            is MainViewModelEvents.DeleteDocument -> {
                viewModelScope.launch {

                    val document = _state.value.selectedDocument

                    if (document != null) {

                        try {

                            repository.deleteObject(document)

                            _state.update {
                                it.copy(
                                    selectedDocument = null,
                                    isDetailSheetOpen = false,
                                    isDropdownMenuOpen = false,
                                    isDeleteDialogOpen = false
                                )
                            }
                        } catch (e: Exception) {
                            _state.value.snackbarHostState.showSnackbar(
                                message = event.message,
                                actionLabel = event.label,
                                duration = SnackbarDuration.Indefinite
                            )
                        }
                    } else {
                        _state.value.snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.label,
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }
            }

            MainViewModelEvents.OnDeleteClick -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isDeleteDialogOpen = true
                        )
                    }
                }
            }

            MainViewModelEvents.DismissDeleteDialog -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isDeleteDialogOpen = false
                        )
                    }
                }
            }

            MainViewModelEvents.OnEditClick -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isEditDialogOpen = true,
                            documentName = _state.value.selectedDocument?.name
                        )
                    }
                }
            }

            MainViewModelEvents.DismissEditDialog -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isEditDialogOpen = false
                        )
                    }
                }
            }

            is MainViewModelEvents.OnNameChanged -> {
                viewModelScope.launch {

                    _state.update {
                        it.copy(
                            documentName = event.value
                        )
                    }
                }
            }

            MainViewModelEvents.SaveChanges -> {
                viewModelScope.launch {

                    val document = _state.value.selectedDocument
                    val documentName = _state.value.documentName



                    if (document != null && documentName != null) {

                        val nDocument = Document().apply {
                            _id = document._id
                            name = documentName
                            uri = document.uri
                        }

                        try {
                            repository.nameObject(document = document, name = documentName)

                            _state.update {
                                it.copy(
                                    documentName = null,
                                    isEditDialogOpen = false,
                                    selectedDocument = nDocument
                                )
                            }
                        } catch (e: Exception) {
                            _state.value.snackbarHostState.showSnackbar(
                                message = "ERROR: $e",
                            )
                        }
                    }
                }
            }
        }
    }

}