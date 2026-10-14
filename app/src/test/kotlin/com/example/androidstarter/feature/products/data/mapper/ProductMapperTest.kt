package com.example.androidstarter.feature.products.data.mapper

import com.example.androidstarter.feature.products.data.remote.ProductDto
import org.junit.Assert.*
import org.junit.Test

class ProductMapperTest {
    @Test fun `trims external values`() {
        val product = ProductDto(" id ", " Name ", " Summary ", " Description ").toDomain()
        assertEquals("id", product.id)
        assertEquals("Name", product.name)
        assertEquals("Summary", product.summary)
    }
    @Test fun `rejects unusable identifiers and names`() {
        for (dto in listOf(ProductDto(" ", "Name", "", ""), ProductDto("id", "", "", ""))) {
            assertThrows(InvalidProductDataException::class.java) { dto.toDomain() }
        }
    }
}
