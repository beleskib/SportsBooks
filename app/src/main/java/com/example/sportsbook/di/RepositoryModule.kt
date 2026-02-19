package com.example.sportsbook.di

import com.example.sportsbook.data.repository.AuthRepositoryImpl
import com.example.sportsbook.data.repository.BookingRepositoryImpl
import com.example.sportsbook.data.repository.CoachRepositoryImpl
import com.example.sportsbook.data.repository.ReviewRepositoryImpl
import com.example.sportsbook.data.repository.SportRepositoryImpl
import com.example.sportsbook.data.repository.TimeSlotRepositoryImpl
import com.example.sportsbook.data.repository.UserRepositoryImpl
import com.example.sportsbook.data.repository.VenueRepositoryImpl
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.ReviewRepository
import com.example.sportsbook.domain.repository.SportRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import com.example.sportsbook.domain.repository.UserRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindSportRepository(impl: SportRepositoryImpl): SportRepository

    @Binds
    @Singleton
    abstract fun bindVenueRepository(impl: VenueRepositoryImpl): VenueRepository

    @Binds
    @Singleton
    abstract fun bindCoachRepository(impl: CoachRepositoryImpl): CoachRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    @Singleton
    abstract fun bindTimeSlotRepository(impl: TimeSlotRepositoryImpl): TimeSlotRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository
}
