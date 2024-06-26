package com.guilherme.documentscanner.presentation

import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guilherme.documentscanner.di.Document
import com.guilherme.documentscanner.di.RealmRepository
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
    val isDetailSheetOpen: Boolean = false
)

sealed interface MainViewModelEvents {
    data class OnDocumentAdded(val value: List<Uri>?) : MainViewModelEvents
    data class OnDocumentClick(val value: Document) : MainViewModelEvents
    data object DismissDetailSheet : MainViewModelEvents
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

        }
    }

}