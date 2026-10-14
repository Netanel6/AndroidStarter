package com.example.androidstarter.feature.products.data

import com.example.androidstarter.core.result.AppError
import com.example.androidstarter.core.result.AppResult
import com.example.androidstarter.feature.products.data.mapper.InvalidProductDataException
import com.example.androidstarter.feature.products.data.mapper.toDomain
import com.example.androidstarter.feature.products.data.remote.ProductsApi
import com.example.androidstarter.feature.products.domain.Product
import com.example.androidstarter.feature.products.domain.ProductsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

internal class ProductsRepositoryImpl @Inject constructor(
    private val api: ProductsApi,
) : ProductsRepository {
    override suspend fun getProducts(): AppResult<List<Product>> = try {
        val response = api.getProducts()
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            AppResult.Failure(when (response.code()) {
                401, 403 -> AppError.Unauthorized
                404 -> AppError.NotFound
                in 400..499 -> AppError.Rejected
                else -> AppError.Server
            })
        } else {
            val products = response.body()?.map { it.toDomain() }
            if (products == null || products.map { it.id }.distinct().size != products.size) {
                AppResult.Failure(AppError.InvalidData)
            } else {
                AppResult.Success(products)
            }
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: IOException) {
        AppResult.Failure(AppError.Network)
    } catch (_: SerializationException) {
        AppResult.Failure(AppError.InvalidData)
    } catch (_: InvalidProductDataException) {
        AppResult.Failure(AppError.InvalidData)
    }
}
