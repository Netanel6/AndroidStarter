package com.example.androidstarter.feature.products.data.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class ProductDto(
    val id: String,
    val name: String,
    val summary: String,
    val description: String,
)
