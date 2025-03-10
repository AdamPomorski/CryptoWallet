package com.project.cryptowallet.core.domain.util

enum class DbError: Error {
    DATABASE_OPEN_FAILED,
    READ_ERROR,
    WRITE_ERROR,
    QUERY_ERROR,
    DATABASE_NOT_FOUND,
    UNKNOWN
}