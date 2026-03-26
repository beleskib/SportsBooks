package com.example.sportsbook.di

import com.example.sportsbook.data.repository.AuthRepositoryImpl
import com.example.sportsbook.data.repository.FavoriteRepositoryImpl
import com.example.sportsbook.data.repository.FriendshipRepositoryImpl
import com.example.sportsbook.data.repository.PartyRepositoryImpl
import com.example.sportsbook.data.repository.PaymentRepositoryImpl
import com.example.sportsbook.data.repository.BookingRepositoryImpl
import com.example.sportsbook.data.repository.CoachRepositoryImpl
import com.example.sportsbook.data.repository.ImageRepositoryImpl
import com.example.sportsbook.data.repository.ReviewRepositoryImpl
import com.example.sportsbook.data.repository.SportRepositoryImpl
import com.example.sportsbook.data.repository.TimeSlotRepositoryImpl
import com.example.sportsbook.data.repository.UserRepositoryImpl
import com.example.sportsbook.data.repository.VenueRepositoryImpl
import com.example.sportsbook.data.repository.MatchRepositoryImpl
import com.example.sportsbook.data.repository.NotificationRepositoryImpl
import com.example.sportsbook.data.repository.AvailablePlayerRepositoryImpl
import com.example.sportsbook.data.repository.StripeConnectRepositoryImpl
import com.example.sportsbook.data.repository.GamificationRepositoryImpl
import com.example.sportsbook.data.repository.CommunityRepositoryImpl
import com.example.sportsbook.data.repository.DashboardRepositoryImpl
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.CommunityRepository
import com.example.sportsbook.domain.repository.DashboardRepository
import com.example.sportsbook.domain.repository.FavoriteRepository
import com.example.sportsbook.domain.repository.FriendshipRepository
import com.example.sportsbook.domain.repository.PartyRepository
import com.example.sportsbook.domain.repository.PaymentRepository
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.ImageRepository
import com.example.sportsbook.domain.repository.ReviewRepository
import com.example.sportsbook.domain.repository.SportRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import com.example.sportsbook.domain.repository.UserRepository
import com.example.sportsbook.domain.repository.VenueRepository
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.NotificationRepository
import com.example.sportsbook.domain.repository.AvailablePlayerRepository
import com.example.sportsbook.domain.repository.StripeConnectRepository
import com.example.sportsbook.domain.repository.GamificationRepository
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
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    @Singleton
    abstract fun bindTimeSlotRepository(impl: TimeSlotRepositoryImpl): TimeSlotRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindFriendshipRepository(impl: FriendshipRepositoryImpl): FriendshipRepository

    @Binds
    @Singleton
    abstract fun bindPartyRepository(impl: PartyRepositoryImpl): PartyRepository

    @Binds
    @Singleton
    abstract fun bindStripeConnectRepository(impl: StripeConnectRepositoryImpl): StripeConnectRepository

    @Binds
    @Singleton
    abstract fun bindAvailablePlayerRepository(impl: AvailablePlayerRepositoryImpl): AvailablePlayerRepository

    @Binds
    @Singleton
    abstract fun bindGamificationRepository(impl: GamificationRepositoryImpl): GamificationRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(impl: CommunityRepositoryImpl): CommunityRepository
}
