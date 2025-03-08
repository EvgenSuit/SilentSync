package com.suit.utility.ui

import android.content.Context
import androidx.annotation.StringRes

sealed class UIText {
    data class StringResource(@StringRes val id: Int, val args: List<Any> = listOf()): UIText()

    fun asString(context: Context) =
        when (this) {
            is StringResource -> context.getString(id, *args.toTypedArray())
        }

}