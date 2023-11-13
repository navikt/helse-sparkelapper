package no.nav.helse.sparkel.infotrygd

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotliquery.Row

fun Row.intOrNullToLocalDate(label: String) = try {
    intOrNull(label)?.takeIf { it >= 10101 }?.toLocalDate()
} catch (err: DateTimeParseException) {
    null
}

fun Row.intToLocalDate(label: String) = int(label).toLocalDate()


private fun Int.toLocalDate() =
    LocalDate.parse(this.toString().padStart(8, '0'), DateTimeFormatter.ofPattern("yyyyMMdd"))
