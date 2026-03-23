package com.example.sportsbook.domain.enums

enum class FavoriteEntityType(val value: String) {
    VENUE("venue"),
    COACH("coach"),
    MATCH("match");

    companion object {
        fun fromValue(value: String): FavoriteEntityType =
            entries.find { it.value == value } ?: VENUE
    }
}
