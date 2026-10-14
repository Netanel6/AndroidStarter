package com.example.androidstarter.feature.products.presentation.details

import androidx.lifecycle.ViewModelStore
import com.example.androidstarter.core.result.AppError
import com.example.androidstarter.core.result.AppResult
import com.example.androidstarter.feature.products.domain.Product
import com.example.androidstarter.feature.products.domain.ProductsRepository
import com.example.androidstarter.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = ViewModelStore()
    private val product = Product("one", "One", "Summary", "Description")
    private fun model(id: String = "one", block: suspend () -> AppResult<List<Product>>) = ProductDetailsViewModel(
        object : ProductsRepository { override suspend fun getProducts() = block() }, id
    ).also { store.put("details", it) }
    @After fun cleanup() { store.clear() }

    @Test fun `loads the selected item by id`() = runTest {
        val vm = model { AppResult.Success(listOf(product)) }
        runCurrent()
        assertEquals(UiState.Data(product), vm.state.value.uiState)
    }
    @Test fun `missing item is an explicit error`() = runTest {
        val vm = model("missing") { AppResult.Success(listOf(product)) }
        runCurrent()
        assertEquals(UiState.Error(AppError.NotFound), vm.state.value.uiState)
    }
    @Test fun `retry recovers and back is transient`() = runTest {
        var failed = true
        val vm = model { if (failed) AppResult.Failure(AppError.Server) else AppResult.Success(listOf(product)) }
        runCurrent()
        assertEquals(UiState.Error(AppError.Server), vm.state.value.uiState)
        failed = false
        vm.onEvent(UiEvent.Retry)
        runCurrent()
        vm.onEvent(UiEvent.BackClicked)
        runCurrent()
        assertEquals(UiAction.NavigateBack, vm.actions.first())
        assertEquals(UiState.Data(product), vm.state.value.uiState)
    }
}
