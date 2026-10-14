package com.example.androidstarter.feature.products.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidstarter.core.result.AppError
import com.example.androidstarter.core.result.AppResult
import com.example.androidstarter.feature.products.domain.ProductsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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

@HiltViewModel(assistedFactory = ProductDetailsViewModel.Factory::class)
class ProductDetailsViewModel @AssistedInject constructor(
    private val repository: ProductsRepository,
    @Assisted private val productId: String,
) : ViewModel() {
    @AssistedFactory
    interface Factory { fun create(productId: String): ProductDetailsViewModel }

    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()
    private val _actions = Channel<UiAction>(Channel.BUFFERED)
    val actions: Flow<UiAction> = _actions.receiveAsFlow()
    private var loadJob: Job? = null

    init { load() }

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.Retry -> load()
            UiEvent.BackClicked -> viewModelScope.launch { _actions.send(UiAction.NavigateBack) }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(uiState = UiState.Loading) }
            val result = repository.getProducts()
            currentCoroutineContext().ensureActive()
            val next = when (result) {
                is AppResult.Success -> result.value.find { it.id == productId }
                    ?.let { UiState.Data(it) } ?: UiState.Error(AppError.NotFound)
                is AppResult.Failure -> UiState.Error(result.error)
            }
            _state.update { it.copy(uiState = next) }
        }
    }
}
