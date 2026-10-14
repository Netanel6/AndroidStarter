package com.example.androidstarter.feature.products.data.remote

import retrofit2.Response
import retrofit2.http.GET

internal interface ProductsApi {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductDto>>
}
