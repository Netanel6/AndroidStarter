package com.example.androidstarter.feature.products.presentation.list

import com.example.androidstarter.core.result.AppError
import com.example.androidstarter.feature.products.domain.Product

data class ScreenState(val uiState: UiState = UiState.Loading)
sealed interface UiState {
    data object Loading : UiState
    data object Empty : UiState
    data class Data(val products: List<Product>) : UiState
    data class Error(val error: AppError) : UiState
}
sealed interface UiEvent {
    data object Retry : UiEvent
    data object Resumed : UiEvent
    data class ProductClicked(val id: String) : UiEvent
}
sealed interface UiAction {
    data class NavigateToDetails(val id: String) : UiAction
}
