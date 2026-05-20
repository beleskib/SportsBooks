package com.example.sportsbook.domain.enums

enum class NotificationType(val value: String, val displayName: String) {
    MATCH_JOIN_REQUEST("match_join_request", "Join Request"),
    MATCH_JOIN_APPROVED("match_join_approved", "Join Approved"),
    MATCH_JOIN_DECLINED("match_join_declined", "Join Declined"),
    MATCH_CHAT_MESSAGE("match_chat_message", "Chat Message"),
    MATCH_STARTING_SOON("match_starting_soon", "Match Starting"),
    MATCH_CANCELLED("match_cancelled", "Match Cancelled"),
    MATCH_LOBBY_FULL("match_lobby_full", "Match Full"),
    BOOKING_REQUEST("booking_request", "Booking Request"),
    BOOKING_APPROVED("booking_approved", "Booking Approved"),
    BOOKING_DECLINED("booking_declined", "Booking Declined"),
    BOOKING_CONFIRMED("booking_confirmed", "Booking Confirmed"),
    BOOKING_CANCELLED("booking_cancelled", "Booking Cancelled"),
    BOOKING_REMINDER("booking_reminder", "Booking Reminder"),
    RATING_RECEIVED("rating_received", "New Rating"),
    FRIEND_REQUEST("friend_request", "Friend Request"),
    FRIEND_REQUEST_ACCEPTED("friend_request_accepted", "Friend Accepted"),
    PARTY_INVITE("party_invite", "Party Invite"),
    PARTY_INVITE_ACCEPTED("party_invite_accepted", "Invite Accepted"),
    PARTY_INVITE_DECLINED("party_invite_declined", "Invite Declined"),
    PARTY_JOINED_MATCH("party_joined_match", "Party Joined Match"),
    PARTY_DISBANDED("party_disbanded", "Party Disbanded"),
    FEED_POST_LIKED("feed_post_liked", "Post Liked"),
    FEED_POST_COMMENTED("feed_post_commented", "Post Comment"),
    MATCH_COMPLETED("match_completed", "Match Completed"),
    MATCH_INVITE("match_invite", "Match Invite"),
    SPLIT_PAYMENT_REQUEST("split_payment_request", "Split Payment"),
    REVIEW_REMINDER("review_reminder", "Rating Reminder"),
    GENERAL("general", "General");

    companion object {
        fun fromValue(value: String): NotificationType =
            entries.find { it.value == value } ?: GENERAL
    }
}
