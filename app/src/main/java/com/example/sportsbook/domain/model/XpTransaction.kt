package com.example.sportsbook.domain.model

data class XpTransaction(
    val id: Long = 0,
    val userId: Long = 0,
    val amount: Int = 0,
    val sourceType: String = "",
    val sourceId: Long? = null,
    val description: String? = null,
    val createdAt: String = ""
)
