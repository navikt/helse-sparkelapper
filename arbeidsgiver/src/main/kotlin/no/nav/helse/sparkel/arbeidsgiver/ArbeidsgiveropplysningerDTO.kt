package no.nav.helse.sparkel.arbeidsgiver

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

internal class ArbeidsgiveropplysningerDTO(
    val type: Meldingstype,
    val organisasjonsnummer: String,
    val fødselsnummer: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    val meldingstype get() = type.name.lowercase().toByteArray()
    internal companion object {
        internal fun JsonMessage.tilArbeidsgiveropplysningerDTO() = ArbeidsgiveropplysningerDTO(
            type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER,
            organisasjonsnummer = this["organisasjonsnummer"].asText(),
            fødselsnummer = this["fødselsnummer"].asText(),
            fom = this["fom"].asLocalDate(),
            tom = this["tom"].asLocalDate(),
            opprettet = this["@opprettet"].asLocalDateTime()
        )
    }
}

internal enum class Meldingstype {
    TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER
}
