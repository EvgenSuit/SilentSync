package com.suit.playreview.impl

import androidx.datastore.core.Serializer
import com.suit.playreview.api.PlayReviewData
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object PlayReviewDataSerializer: Serializer<PlayReviewData> {
    override val defaultValue: PlayReviewData
        get() = PlayReviewData()

    override suspend fun readFrom(input: InputStream): PlayReviewData {
        return Json.decodeFromString(
            PlayReviewData.serializer(), input.readBytes().decodeToString()
        )
    }

    override suspend fun writeTo(
        t: PlayReviewData,
        output: OutputStream
    ) {
        output.write(
            Json.encodeToString(PlayReviewData.serializer(), t)
                .encodeToByteArray()
        )
    }
}