package com.project.cryptowallet.core.presentation.util

import android.content.Context
import com.project.cryptowallet.R
import com.project.cryptowallet.core.domain.util.DbError
import com.project.cryptowallet.core.domain.util.Error
import com.project.cryptowallet.core.domain.util.NetworkError

fun Error.toUserFriendlyString(context: Context): String {
    return when (this) {
        is NetworkError -> this.toString(context)
        is DbError -> this.toString(context)
        else -> context.getString(R.string.error_unknown) // Fallback for unexpected errors
    }
}