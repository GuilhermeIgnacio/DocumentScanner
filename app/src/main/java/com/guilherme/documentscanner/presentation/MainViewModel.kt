package com.guilherme.documentscanner.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guilherme.documentscanner.di.RealmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainViewState(
    val uriList: List<Uri> = emptyList()
)

sealed interface MainViewModelEvents {
    data class OnDocumentAdded(val value: List<Uri>?) : MainViewModelEvents
}

class MainViewModel(
    private val repository: RealmRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainViewState())
    val state = _state.asStateFlow()

    fun onEvent(event: MainViewModelEvents) {
        when (event) {
            is MainViewModelEvents.OnDocumentAdded -> {
                viewModelScope.launch {

                    val list = event.value

                    if (list != null) {

                        _state.update {
                            it.copy(
                                uriList = list
                            )
                        }
                    }
                }
            }
        }
    }

}