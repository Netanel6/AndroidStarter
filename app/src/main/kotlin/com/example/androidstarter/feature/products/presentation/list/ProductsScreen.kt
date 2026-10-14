package com.example.androidstarter.feature.products.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidstarter.R
import com.example.androidstarter.core.designsystem.StarterTheme
import com.example.androidstarter.feature.products.domain.Product
import com.example.androidstarter.feature.products.presentation.components.CatalogLoading
import com.example.androidstarter.feature.products.presentation.components.CatalogMessage
import com.example.androidstarter.feature.products.presentation.components.displayMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(state: ScreenState, onEvent: (UiEvent) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { padding ->
        val body = Modifier.fillMaxSize().padding(padding)
        when (val ui = state.uiState) {
            UiState.Loading -> CatalogLoading(body)
            UiState.Empty -> CatalogMessage(stringResource(R.string.empty_title), stringResource(R.string.empty_message),
                { onEvent(UiEvent.Retry) }, body)
            is UiState.Error -> CatalogMessage(stringResource(R.string.error_title), ui.error.displayMessage(),
                { onEvent(UiEvent.Retry) }, body)
            is UiState.Data -> LazyColumn(body, contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.sample_catalog), color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.catalog_title), modifier = Modifier.testTag("catalog-title"),
                            style = MaterialTheme.typography.displaySmall)
                        Text(stringResource(R.string.catalog_intro), style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp))
                    }
                }
                itemsIndexed(ui.products, key = { _, product -> product.id }) { index, product ->
                    Card(onClick = { onEvent(UiEvent.ProductClicked(product.id)) },
                        modifier = Modifier.fillMaxWidth().testTag("product-${product.id}"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Text((index + 1).toString().padStart(2, '0'), style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(product.name, style = MaterialTheme.typography.titleLarge)
                                Text(product.summary, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item { Text(stringResource(R.string.catalog_footer), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductsPreview() {
    StarterTheme { ProductsScreen(ScreenState(UiState.Data(listOf(Product("preview", "Field notes", "Room for your next idea.", ""))))) {} }
}
