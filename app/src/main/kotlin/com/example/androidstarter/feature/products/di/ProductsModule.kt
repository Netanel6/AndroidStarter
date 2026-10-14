package com.example.androidstarter.feature.products.di

import com.example.androidstarter.BuildConfig
import com.example.androidstarter.feature.products.data.ProductsRepositoryImpl
import com.example.androidstarter.feature.products.data.remote.DemoProductsApi
import com.example.androidstarter.feature.products.data.remote.ProductsApi
import com.example.androidstarter.feature.products.domain.ProductsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProductsModule {
    @Binds
    abstract fun repository(implementation: ProductsRepositoryImpl): ProductsRepository

    companion object {
        @Provides
        fun api(retrofit: Retrofit, demo: DemoProductsApi): ProductsApi =
            if (BuildConfig.DEMO_MODE) demo else retrofit.create(ProductsApi::class.java)
    }
}
