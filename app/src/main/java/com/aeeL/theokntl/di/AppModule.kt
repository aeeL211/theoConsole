package com.aeeL.theokntl.di

import com.aeeL.theokntl.core.ConsoleConstants
import com.aeeL.theokntl.data.repository.ConsoleRepository
import com.aeeL.theokntl.data.store.AppPreferences
import com.aeeL.theokntl.domain.feature.PurchaseStampStore
import com.aeeL.theokntl.domain.gateway.ConsoleGateway
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    @Singleton
    abstract fun bindConsoleGateway(repository: ConsoleRepository): ConsoleGateway

    @Binds
    @Singleton
    abstract fun bindPurchaseStampStore(preferences: AppPreferences): PurchaseStampStore
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val timeout = ConsoleConstants.REQUEST_TIMEOUT_MS
        return OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .callTimeout(timeout, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
