package com.example.androidstarter.feature.products.domain

import com.example.androidstarter.core.result.AppResult

interface ProductsRepository {
    suspend fun getProducts(): AppResult<List<Product>>
}
