package com.example.androidstarter.feature.products.data.mapper

import com.example.androidstarter.feature.products.data.remote.ProductDto
import com.example.androidstarter.feature.products.domain.Product

internal class InvalidProductDataException : Exception()

internal fun ProductDto.toDomain(): Product {
    if (id.isBlank() || name.isBlank()) throw InvalidProductDataException()
    return Product(id.trim(), name.trim(), summary.trim(), description.trim())
}
