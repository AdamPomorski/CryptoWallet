package com.project.cryptowallet.core.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.toLocalDate(): LocalDate {
    val pattern = "yyyy-MM-dd"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return LocalDate.parse(this,formatter)

}