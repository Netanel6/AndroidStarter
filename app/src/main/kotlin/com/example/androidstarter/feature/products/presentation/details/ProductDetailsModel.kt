package com.example.androidstarter.feature.products.presentation.details

import com.example.androidstarter.core.result.AppError
import com.example.androidstarter.feature.products.domain.Product

data class ScreenState(val uiState: UiState = UiState.Loading)
sealed interface UiState {
    data object Loading : UiState
    data class Data(val product: Product) : UiState
    data class Error(val error: AppError) : UiState
}
sealed interface UiEvent {
    data object Retry : UiEvent
    data object BackClicked : UiEvent
}
sealed interface UiAction { data object NavigateBack : UiAction }
