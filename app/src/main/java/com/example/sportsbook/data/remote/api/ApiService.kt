package com.example.sportsbook.data.remote.api

import com.example.sportsbook.data.remote.dto.ApiResponseDto
import com.example.sportsbook.data.remote.dto.BookingDto
import com.example.sportsbook.data.remote.dto.CoachDto
import com.example.sportsbook.data.remote.dto.CreateBookingRequestDto
import com.example.sportsbook.data.remote.dto.CreateReviewRequestDto
import com.example.sportsbook.data.remote.dto.CreateUserRequestDto
import com.example.sportsbook.data.remote.dto.PaginatedResponseDto
import com.example.sportsbook.data.remote.dto.ReviewDto
import com.example.sportsbook.data.remote.dto.SetRoleRequestDto
import com.example.sportsbook.data.remote.dto.SportCategoryDto
import com.example.sportsbook.data.remote.dto.TimeSlotDto
import com.example.sportsbook.data.remote.dto.UpdateUserRequestDto
import com.example.sportsbook.data.remote.dto.UserDto
import com.example.sportsbook.data.remote.dto.VenueDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: CreateUserRequestDto): ApiResponseDto<UserDto>

    // User
    @GET("api/users/me")
    suspend fun getProfile(): ApiResponseDto<UserDto>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateUserRequestDto): ApiResponseDto<UserDto>

    @PUT("api/users/me/role")
    suspend fun setRole(@Body request: SetRoleRequestDto): ApiResponseDto<UserDto>

    // Sports
    @GET("api/sports")
    suspend fun getSports(): ApiResponseDto<List<SportCategoryDto>>

    @GET("api/sports/{id}")
    suspend fun getSportById(@Path("id") id: Long): ApiResponseDto<SportCategoryDto>

    // Venues
    @GET("api/venues/by-sport/{sportType}")
    suspend fun getVenuesBySport(@Path("sportType") sportType: String): ApiResponseDto<List<VenueDto>>

    @GET("api/venues/{id}")
    suspend fun getVenueById(@Path("id") id: Long): ApiResponseDto<VenueDto>

    @GET("api/venues/top-deals")
    suspend fun getTopDealVenues(): ApiResponseDto<List<VenueDto>>

    @GET("api/venues/search")
    suspend fun searchVenues(@Query("q") query: String): ApiResponseDto<List<VenueDto>>

    @GET("api/venues/mine")
    suspend fun getMyVenues(): ApiResponseDto<List<VenueDto>>

    // Coaches
    @GET("api/coaches/by-sport/{sportType}")
    suspend fun getCoachesBySport(@Path("sportType") sportType: String): ApiResponseDto<List<CoachDto>>

    @GET("api/coaches/{id}")
    suspend fun getCoachById(@Path("id") id: Long): ApiResponseDto<CoachDto>

    @GET("api/coaches/top-deals")
    suspend fun getTopDealCoaches(): ApiResponseDto<List<CoachDto>>

    @GET("api/coaches/search")
    suspend fun searchCoaches(@Query("q") query: String): ApiResponseDto<List<CoachDto>>

    @GET("api/coaches/mine")
    suspend fun getMyCoachProfile(): ApiResponseDto<CoachDto?>

    // Time Slots
    @GET("api/venues/{venueId}/time-slots")
    suspend fun getVenueTimeSlots(
        @Path("venueId") venueId: Long,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): ApiResponseDto<List<TimeSlotDto>>

    @GET("api/coaches/{coachId}/time-slots")
    suspend fun getCoachTimeSlots(
        @Path("coachId") coachId: Long,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): ApiResponseDto<List<TimeSlotDto>>

    @GET("api/time-slots/{id}")
    suspend fun getTimeSlotById(@Path("id") id: Long): ApiResponseDto<TimeSlotDto>

    // Bookings
    @POST("api/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequestDto): ApiResponseDto<BookingDto>

    @GET("api/bookings/mine")
    suspend fun getMyBookings(@Query("status") status: String? = null): ApiResponseDto<List<BookingDto>>

    @GET("api/bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): ApiResponseDto<BookingDto>

    @PUT("api/bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: Long,
        @Body request: Map<String, String>
    ): ApiResponseDto<BookingDto>

    @GET("api/bookings/partner")
    suspend fun getPartnerBookings(@Query("status") status: String? = null): ApiResponseDto<List<BookingDto>>

    // Reviews
    @GET("api/reviews/venue/{venueId}")
    suspend fun getVenueReviews(@Path("venueId") venueId: Long): ApiResponseDto<List<ReviewDto>>

    @GET("api/reviews/coach/{coachId}")
    suspend fun getCoachReviews(@Path("coachId") coachId: Long): ApiResponseDto<List<ReviewDto>>

    @POST("api/reviews")
    suspend fun createReview(@Body request: CreateReviewRequestDto): ApiResponseDto<ReviewDto>

    @GET("api/reviews/mine")
    suspend fun getMyReviews(): ApiResponseDto<List<ReviewDto>>
}
