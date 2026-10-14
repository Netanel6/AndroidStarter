package com.example.androidstarter.feature.products.data.remote

import retrofit2.Response
import javax.inject.Inject

/** Offline sample data. Replaced by the Retrofit implementation when a base URL is configured. */
internal class DemoProductsApi @Inject constructor() : ProductsApi {
    override suspend fun getProducts(): Response<List<ProductDto>> = Response.success(
        listOf(
            ProductDto("field-notes", "Field notes", "Room for your next idea.",
                "A simple notebook for observations, sketches, and things worth remembering. This is sample content you can replace with your own product."),
            ProductDto("trail-kit", "Trail kit", "A little more adventure.",
                "The essentials for a day outside, gathered in one place. Use this sample item to explore the catalog and detail screens."),
            ProductDto("daily-planner", "Daily planner", "Make space for what matters.",
                "A quiet place to organize the day and choose the next step. Replace this sample catalog when you build your own app."),
        )
    )
}
