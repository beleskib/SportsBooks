package com.example.sportsbook.di

import com.example.sportsbook.data.remote.api.V2ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// ============================================================
// v2-practical-ux: Provides V2ApiService using the shared Retrofit
// instance already built by NetworkModule. No re-provision of
// OkHttp, Json, or Retrofit — those are singletons.
// ============================================================

@Module
@InstallIn(SingletonComponent::class)
object V2NetworkModule {

    @Provides
    @Singleton
    fun provideV2ApiService(retrofit: Retrofit): V2ApiService =
        retrofit.create(V2ApiService::class.java)
}
