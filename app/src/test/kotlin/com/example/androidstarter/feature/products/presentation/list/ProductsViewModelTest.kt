package com.example.androidstarter.feature.products.presentation.list

import androidx.lifecycle.ViewModelStore
import com.example.androidstarter.core.result.AppError
import com.example.androidstarter.core.result.AppResult
import com.example.androidstarter.feature.products.domain.Product
import com.example.androidstarter.feature.products.domain.ProductsRepository
import com.example.androidstarter.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val store = ViewModelStore()
    private val product = Product("one", "One", "Summary", "Description")
    private fun model(block: suspend () -> AppResult<List<Product>>) = ProductsViewModel(
        object : ProductsRepository { override suspend fun getProducts() = block() }
    ).also { store.put("products", it) }
    @After fun cleanup() { store.clear() }

    @Test fun `initial load transitions to data`() = runTest {
        val vm = model { AppResult.Success(listOf(product)) }
        assertEquals(UiState.Loading, vm.state.value.uiState)
        runCurrent()
        assertEquals(UiState.Data(listOf(product)), vm.state.value.uiState)
    }

    @Test fun `empty response renders empty`() = runTest {
        val vm = model { AppResult.Success(emptyList()) }
        runCurrent()
        assertEquals(UiState.Empty, vm.state.value.uiState)
    }

    @Test fun `retry recovers from failure`() = runTest {
        var calls = 0
        val vm = model { if (calls++ == 0) AppResult.Failure(AppError.Network) else AppResult.Success(listOf(product)) }
        runCurrent()
        assertEquals(UiState.Error(AppError.Network), vm.state.value.uiState)
        vm.onEvent(UiEvent.Retry)
        runCurrent()
        assertEquals(UiState.Data(listOf(product)), vm.state.value.uiState)
        assertEquals(2, calls)
    }

    @Test fun `cancelled stale load cannot overwrite latest result`() = runTest {
        val stale = CompletableDeferred<AppResult<List<Product>>>()
        var calls = 0
        val vm = model {
            if (calls++ == 0) withContext(NonCancellable) { stale.await() }
            else AppResult.Success(listOf(product))
        }
        runCurrent()
        vm.onEvent(UiEvent.Retry)
        runCurrent()
        stale.complete(AppResult.Failure(AppError.Server))
        runCurrent()
        assertEquals(UiState.Data(listOf(product)), vm.state.value.uiState)
    }

    @Test fun `click emits separate action and suppresses duplicates until resumed`() = runTest {
        val vm = model { AppResult.Success(listOf(product)) }
        runCurrent()
        val actions = mutableListOf<UiAction>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.actions.toList(actions) }
        vm.onEvent(UiEvent.ProductClicked("missing"))
        vm.onEvent(UiEvent.ProductClicked("one"))
        vm.onEvent(UiEvent.ProductClicked("one"))
        runCurrent()
        assertEquals(listOf(UiAction.NavigateToDetails("one")), actions)
        assertEquals(UiState.Data(listOf(product)), vm.state.value.uiState)
        vm.onEvent(UiEvent.Resumed)
        vm.onEvent(UiEvent.ProductClicked("one"))
        runCurrent()
        assertEquals(2, actions.size)
    }

    @Test fun `buffered navigation waits for a collector without changing state`() = runTest {
        val vm = model { AppResult.Success(listOf(product)) }
        runCurrent()
        vm.onEvent(UiEvent.ProductClicked("one"))
        runCurrent()
        val actions = mutableListOf<UiAction>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.actions.toList(actions) }
        assertEquals(listOf(UiAction.NavigateToDetails("one")), actions)
    }

    @Test fun `clearing ViewModel cancels its request`() = runTest {
        val cancelled = CompletableDeferred<Unit>()
        val vm = model {
            try { CompletableDeferred<AppResult<List<Product>>>().await() }
            finally { cancelled.complete(Unit) }
        }
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(cancelled.isCompleted)
        assertEquals(UiState.Loading, vm.state.value.uiState)
    }
}
