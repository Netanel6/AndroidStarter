package com.example.androidstarter.feature.products.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
fun ProductDetailsScreen(state: ScreenState, onEvent: (UiEvent) -> Unit) {
    Scaffold(topBar = { TopAppBar(
        title = { Text(stringResource(R.string.details_title)) },
        navigationIcon = { TextButton(onClick = { onEvent(UiEvent.BackClicked) }) { Text(stringResource(R.string.back)) } },
    ) }) { padding ->
        val body = Modifier.fillMaxSize().padding(padding)
        when (val ui = state.uiState) {
            UiState.Loading -> CatalogLoading(body)
            is UiState.Error -> CatalogMessage(stringResource(R.string.error_title), ui.error.displayMessage(),
                { onEvent(UiEvent.Retry) }, body)
            is UiState.Data -> Column(body.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(stringResource(R.string.about_item), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium)
                Text(ui.product.name, modifier = Modifier.testTag("details-name"), style = MaterialTheme.typography.displaySmall)
                Text(ui.product.summary, style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(ui.product.description, style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { onEvent(UiEvent.BackClicked) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.return_catalog))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsPreview() {
    StarterTheme { ProductDetailsScreen(ScreenState(UiState.Data(Product("preview", "Field notes", "Room for your next idea.", "A place for things worth remembering.")))) {} }
}
