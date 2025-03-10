package com.project.cryptowallet.core.presentation.util

import android.content.Context
import com.project.cryptowallet.R
import com.project.cryptowallet.core.domain.util.DbError

fun DbError.toString(context: Context): String{
    val resId =  when(this){
        DbError.DATABASE_OPEN_FAILED -> R.string.error_db_open_failed
        DbError.READ_ERROR -> R.string.error_db_read
        DbError.WRITE_ERROR -> R.string.error_db_write
        DbError.QUERY_ERROR -> R.string.error_db_query
        DbError.DATABASE_NOT_FOUND -> R.string.error_db_not_found
        DbError.UNKNOWN -> R.string.error_unknown
    }

    return context.getString(resId)
}