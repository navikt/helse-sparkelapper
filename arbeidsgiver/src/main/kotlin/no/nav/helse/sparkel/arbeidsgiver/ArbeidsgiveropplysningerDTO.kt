package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime

internal data class ArbeidsgiveropplysningerDTO(
    val type: Meldingstype,
    val organisasjonsnummer: String,
    val fødselsnummer: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val arbeidgiveropplysninger: OpplysningerDTO,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    constructor(message: JsonMessage) : this(
        type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER,
        organisasjonsnummer = message["organisasjonsnummer"].asText(),
        fødselsnummer = message["fødselsnummer"].asText(),
        fom = message["fom"].asLocalDate(),
        tom = message["tom"].asLocalDate(),
        arbeidgiveropplysninger = OpplysningerDTO(message["arbeidgiveropplysninger"]),
        opprettet = message["@opprettet"].asLocalDateTime()
    )
}

internal class OpplysningerDTO(
    val periode: String?,
    val refusjon: String?,
    val inntekt: String?
) {
    constructor(message: JsonNode) : this(
        periode = message["periode"]?.asText(),
        refusjon = message["refusjon"]?.asText(),
        inntekt = message["inntekt"]?.asText()
    )
}