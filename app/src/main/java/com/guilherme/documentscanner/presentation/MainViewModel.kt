package com.guilherme.documentscanner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guilherme.documentscanner.di.RealmRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: RealmRepository
): ViewModel() {

    fun sayHello() {
        viewModelScope.launch {
            repository.doSomething()
        }
    }

}