package com.suit.playreview.api

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class PlayReviewData(
    val didShow: Boolean = false,
    @Serializable(with = InstantSerializer::class) val installTime: Instant? = null
)