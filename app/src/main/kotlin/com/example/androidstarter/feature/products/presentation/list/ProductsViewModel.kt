package com.example.androidstarter.feature.products.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidstarter.core.result.AppResult
import com.example.androidstarter.feature.products.domain.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(private val repository: ProductsRepository) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()
    private val _actions = Channel<UiAction>(Channel.BUFFERED)
    val actions: Flow<UiAction> = _actions.receiveAsFlow()
    private var loadJob: Job? = null
    private var navigationPending = false

    init { load() }

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.Retry -> load()
            UiEvent.Resumed -> navigationPending = false
            is UiEvent.ProductClicked -> {
                val products = (_state.value.uiState as? UiState.Data)?.products.orEmpty()
                if (!navigationPending && products.any { it.id == event.id }) {
                    navigationPending = true
                    viewModelScope.launch { _actions.send(UiAction.NavigateToDetails(event.id)) }
                }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(uiState = UiState.Loading) }
            val result = repository.getProducts()
            currentCoroutineContext().ensureActive()
            _state.update { it.copy(uiState = when (result) {
                is AppResult.Success -> if (result.value.isEmpty()) UiState.Empty else UiState.Data(result.value.toList())
                is AppResult.Failure -> UiState.Error(result.error)
            }) }
        }
    }
}
