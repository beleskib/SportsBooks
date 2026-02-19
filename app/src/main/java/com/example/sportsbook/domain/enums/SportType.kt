package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SportType {
    @SerialName("basketball") BASKETBALL,
    @SerialName("football") FOOTBALL,
    @SerialName("tennis") TENNIS,
    @SerialName("paddle") PADDLE,
    @SerialName("volleyball") VOLLEYBALL,
    @SerialName("swimming") SWIMMING,
    @SerialName("boxing") BOXING,
    @SerialName("mma") MMA,
    @SerialName("yoga") YOGA,
    @SerialName("pilates") PILATES,
    @SerialName("crossfit") CROSSFIT,
    @SerialName("running") RUNNING,
    @SerialName("cycling") CYCLING,
    @SerialName("golf") GOLF,
    @SerialName("badminton") BADMINTON,
    @SerialName("table_tennis") TABLE_TENNIS,
    @SerialName("handball") HANDBALL,
    @SerialName("baseball") BASEBALL,
    @SerialName("cricket") CRICKET;

    val displayName: String
        get() = name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
}
