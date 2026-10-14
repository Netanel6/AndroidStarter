package com.example.androidstarter.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.androidstarter.feature.products.presentation.details.ProductDetailsRoute
import com.example.androidstarter.feature.products.presentation.details.ProductDetailsViewModel
import com.example.androidstarter.feature.products.presentation.list.ProductsRoute
import kotlinx.serialization.Serializable
import com.example.androidstarter.feature.products.presentation.details.UiAction as DetailsAction
import com.example.androidstarter.feature.products.presentation.list.UiAction as ProductsAction

@Serializable
internal data object Catalog : NavKey

@Serializable
internal data class ProductDetails(val id: String) : NavKey

@Composable
fun StarterNavigation() {
    val backStack = rememberNavBackStack(Catalog)
    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Catalog> {
                ProductsRoute(hiltViewModel()) { action ->
                    when (action) {
                        is ProductsAction.NavigateToDetails -> if (backStack.lastOrNull() == Catalog) {
                            backStack.add(ProductDetails(action.id))
                        }
                    }
                }
            }
            entry<ProductDetails> { destination ->
                val viewModel = hiltViewModel<ProductDetailsViewModel, ProductDetailsViewModel.Factory>(
                    creationCallback = { it.create(destination.id) },
                )
                ProductDetailsRoute(viewModel) { action ->
                    when (action) {
                        DetailsAction.NavigateBack -> if (backStack.lastOrNull() == destination) {
                            backStack.removeLastOrNull()
                        }
                    }
                }
            }
        },
    )
}
