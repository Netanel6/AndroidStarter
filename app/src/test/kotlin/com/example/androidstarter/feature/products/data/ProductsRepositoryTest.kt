package com.example.androidstarter.feature.products.data

import com.example.androidstarter.core.result.AppError
import com.example.androidstarter.core.result.AppResult
import com.example.androidstarter.feature.products.data.remote.ProductDto
import com.example.androidstarter.feature.products.data.remote.ProductsApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException

class ProductsRepositoryTest {
    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var repository: ProductsRepositoryImpl
    private val item = """{"id":"one","name":"One","summary":"Summary","description":"Description","extra":true}"""
    @Before fun setup() {
        server.start()
        val api = Retrofit.Builder().baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build().create(ProductsApi::class.java)
        repository = ProductsRepositoryImpl(api)
    }
    @After fun cleanup() { server.shutdown() }
    private fun response(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))
    }
    @Test fun `real Retrofit decoding maps DTO and ignores extra fields`() = runTest {
        response("[$item]")
        val result = repository.getProducts() as AppResult.Success
        assertEquals("One", result.value.single().name)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/products", request.path)
    }
    @Test fun `empty list remains success`() = runTest {
        response("[]")
        assertEquals(AppResult.Success(emptyList<ProductDto>()), repository.getProducts())
    }
    @Test fun `HTTP errors map without exposing bodies`() = runTest {
        for ((code, error) in mapOf(401 to AppError.Unauthorized, 403 to AppError.Unauthorized,
            404 to AppError.NotFound, 422 to AppError.Rejected, 503 to AppError.Server)) {
            response("private server detail", code)
            assertEquals(AppResult.Failure(error), repository.getProducts())
        }
    }
    @Test fun `empty body malformed fields and duplicate ids are invalid data`() = runTest {
        response("", 204)
        assertEquals(AppResult.Failure(AppError.InvalidData), repository.getProducts())
        for (payload in listOf("{}", """[{"id":"one"}]""", "[$item,$item]", "[${item.replace("one", " ")} ]")) {
            response(payload)
            assertEquals(AppResult.Failure(AppError.InvalidData), repository.getProducts())
        }
    }
    @Test fun `IO errors map to network failure`() = runTest {
        val api = object : ProductsApi {
            override suspend fun getProducts(): Response<List<ProductDto>> = throw IOException("offline")
        }
        assertEquals(AppResult.Failure(AppError.Network), ProductsRepositoryImpl(api).getProducts())
    }
    @Test fun `cancellation propagates instead of becoming failure`() = runTest {
        val api = object : ProductsApi {
            override suspend fun getProducts(): Response<List<ProductDto>> = throw CancellationException("cancel")
        }
        try {
            ProductsRepositoryImpl(api).getProducts()
            fail("Cancellation was swallowed")
        } catch (expected: CancellationException) {
            assertEquals("cancel", expected.message)
        }
    }
    @Test fun `programming defects are not disguised as network errors`() = runTest {
        val api = object : ProductsApi {
            override suspend fun getProducts(): Response<List<ProductDto>> = error("defect")
        }
        try {
            ProductsRepositoryImpl(api).getProducts()
            fail("Programming defect was swallowed")
        } catch (expected: IllegalStateException) {
            assertEquals("defect", expected.message)
        }
    }
}
