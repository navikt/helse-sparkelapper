package no.nav.helse.sparkel.inntekt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.navikt.tbd_libs.retry.retry
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.time.YearMonth
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

class InntektRestClient(
    private val baseUrl: String,
    private val inntektskomponentenOAuthScope: String,
    private val httpClient: HttpClient,
    private val tokenSupplier: TokenSupplier,
) {
    suspend fun hentInntektsliste(
        fnr: String,
        fom: YearMonth,
        tom: YearMonth,
        filter: String,
        callId: String,
        orgnummer: String? = null
    ) = retry {
        httpClient.preparePost("$baseUrl/api/v1/hentinntektliste") {
            expectSuccess = true
            header("Authorization", "Bearer ${tokenSupplier(inntektskomponentenOAuthScope)}")
            header("Nav-Consumer-Id", "sparkel-inntekt")
            header("Nav-Call-Id", callId)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                mapOf(
                    // om sender inn aktørid til inntektskomponenten så kan vi få aktørid tilbake noen steder.
                    // Om vi sender inn fnr vil vi aldri få aktørid tilbake
                    // !! Viktig at vi derfor kun sender FNR !!
                    "ident" to mapOf(
                        "identifikator" to fnr,
                        "aktoerType" to "NATURLIG_IDENT"
                    ),
                    "ainntektsfilter" to filter,
                    "formaal" to "Sykepenger",
                    "maanedFom" to fom,
                    "maanedTom" to tom
                )
            )
        }.execute {
            val content = it.bodyAsText()
            sikkerlogg.info(
                "inntektskomponenten svarte for filter=$filter med:\n\t$content",
                keyValue("callId", callId),
                keyValue("fødselsnummer", fnr)
            )
            tilMånedListe(objectMapper.readValue(content), orgnummer)
        }
    }
}

private fun tilMånedListe(node: JsonNode, orgnummer: String? = null) = node.path("arbeidsInntektMaaned")
    .map { tilMåned(it, orgnummer) }

private fun tilInntekt(node: JsonNode, inntekterForOrgnummer: String? = null): Inntekt? {
    check(identifikator(node, "AKTOER_ID") == null) {
        "Vi skal ikke få aktørID fra inntektskomponenten"
    }
    val orgnr = identifikator(node, "ORGANISASJON")
    if (inntekterForOrgnummer != null && inntekterForOrgnummer != orgnr) return null
    return Inntekt(
        beløp = node["beloep"].asDouble(),
        inntektstype = Inntektstype.valueOf(node["inntektType"].textValue()),
        orgnummer = orgnr,
        fødselsnummer = identifikator(node, "NATURLIG_IDENT"),
        beskrivelse = node["beskrivelse"].textValue(),
        fordel = node["fordel"].textValue()
    )
}

private fun identifikator(node: JsonNode, type: String) =
    node["virksomhet"].takeIf { it["aktoerType"].asText() == type }?.get("identifikator")?.asText()

private fun tilMåned(node: JsonNode, orgnummer: String?) = Måned(
    årMåned = YearMonth.parse(node["aarMaaned"].asText()),
    inntektsliste = node.path("arbeidsInntektInformasjon").path("inntektListe").mapNotNull { tilInntekt(it, orgnummer) }
)

data class Måned(
    val årMåned: YearMonth,
    val inntektsliste: List<Inntekt>
)
data class Inntekt(
    val beløp: Double,
    val inntektstype: Inntektstype,
    val orgnummer: String?,
    val fødselsnummer: String?,
    val beskrivelse: String?,
    val fordel: String?
) {
    init {
        check(orgnummer != null || fødselsnummer != null) {
            "Verken orgnummer ELLER fødselsnummer er tilstede i inntekten!"
        }
    }
}

enum class Inntektstype {
    LOENNSINNTEKT,
    NAERINGSINNTEKT,
    PENSJON_ELLER_TRYGD,
    YTELSE_FRA_OFFENTLIGE
}
