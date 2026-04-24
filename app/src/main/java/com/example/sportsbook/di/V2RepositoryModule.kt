package com.example.sportsbook.di

import com.example.sportsbook.data.repository.V2RepositoryImpl
import com.example.sportsbook.domain.repository.V2Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ============================================================
// v2-practical-ux: Binds V2Repository — kept separate from
// RepositoryModule.kt so the diff stays localized to v2 files.
// ============================================================

@Module
@InstallIn(SingletonComponent::class)
abstract class V2RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindV2Repository(impl: V2RepositoryImpl): V2Repository
}
