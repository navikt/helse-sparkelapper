package no.nav.helse.sparkel.inntekt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.prometheus.client.Summary
import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.inntekt.Inntekter.Type.InntekterForSykepengegrunnlag
import java.time.YearMonth
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

private const val INNTEKTSKOMPONENT_CLIENT_SECONDS_METRICNAME = "inntektskomponent_client_seconds"
private val clientLatencyStats: Summary = Summary.build()
    .name(INNTEKTSKOMPONENT_CLIENT_SECONDS_METRICNAME)
    .quantile(0.5, 0.05) // Add 50th percentile (= median) with 5% tolerated error
    .quantile(0.9, 0.01) // Add 90th percentile with 1% tolerated error
    .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
    .help("Latency inntektskomponenten, in seconds")
    .register()

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

class InntektRestClient(
    private val baseUrl: String,
    private val inntektskomponentenOAuthScope: String,
    private val httpClient: HttpClient,
    private val tokenSupplier: TokenSupplier,
) {
    fun hentInntektsliste(
        fnr: String,
        fom: YearMonth,
        tom: YearMonth,
        filter: String,
        callId: String
    ) = clientLatencyStats.startTimer().use {
        runBlocking {
            httpClient.preparePost("$baseUrl/api/v1/hentinntektliste") {
                header("Authorization", "Bearer ${tokenSupplier(inntektskomponentenOAuthScope)}")
                header("Nav-Consumer-Id", "sparkel-inntekt")
                header("Nav-Call-Id", callId)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(mapOf(
                    "ident" to mapOf(
                        "identifikator" to fnr,
                        "aktoerType" to "NATURLIG_IDENT"
                    ),
                    "ainntektsfilter" to filter,
                    "formaal" to "Sykepenger",
                    "maanedFom" to fom,
                    "maanedTom" to tom
                ))
            }.execute {
                val content = it.bodyAsText()
                sikkerlogg.info("inntektskomponenten svarte for filter=$filter med:\n\t$content", keyValue("callId", callId), keyValue("fødselsnummer", fnr))
                tilMånedListe(objectMapper.readValue(content), filter)
            }
        }
    }
}

private fun tilMånedListe(node: JsonNode, filter: String) = node.path("arbeidsInntektMaaned").map { tilMåned(it, filter) }

private fun tilInntekt(node: JsonNode) = Inntekt(
    beløp = node["beloep"].asDouble(),
    inntektstype = Inntektstype.valueOf(node["inntektType"].textValue()),
    orgnummer = identifikator(node, "ORGANISASJON"),
    fødselsnummer = identifikator(node, "NATURLIG_IDENT"),
    aktørId = identifikator(node, "AKTOER_ID"),
    beskrivelse = node["beskrivelse"].textValue(),
    fordel = node["fordel"].textValue()
)

private fun identifikator(node: JsonNode, type: String) =
    node["virksomhet"].takeIf { it["aktoerType"].asText() == type }?.get("identifikator")?.asText()

private fun tilMåned(node: JsonNode, filter: String) = Måned(
    YearMonth.parse(node["aarMaaned"].asText()),
    tilArbeidsforholdliste(filter, node),
    node.path("arbeidsInntektInformasjon").path("inntektListe").map(::tilInntekt)
)

private fun tilArbeidsforholdliste(
    filter: String,
    node: JsonNode
) = if (filter == InntekterForSykepengegrunnlag.ainntektfilter) {
    node.path("arbeidsInntektInformasjon").path("arbeidsforholdListe").mapNotNull(::tilArbeidsforhold)
} else emptyList()

private fun tilArbeidsforhold(node: JsonNode) = Arbeidsforhold(
    node.getOptional("arbeidsforholdstype")?.asText(),
    node.getOptional("arbeidsgiver")?.getOptional("identifikator")?.asText()
)

private fun JsonNode.getOptional(key: String) = this.takeIf { it.hasNonNull(key) }?.get(key)

data class Måned(
    val årMåned: YearMonth,
    val arbeidsforholdliste: List<Arbeidsforhold>,
    val inntektsliste: List<Inntekt>
)
data class Arbeidsforhold(
    val type: String?,
    val orgnummer: String?
)
data class Inntekt(
    val beløp: Double,
    val inntektstype: Inntektstype,
    val orgnummer: String?,
    val fødselsnummer: String?,
    val aktørId: String?,
    val beskrivelse: String?,
    val fordel: String?
)

enum class Inntektstype {
    LOENNSINNTEKT,
    NAERINGSINNTEKT,
    PENSJON_ELLER_TRYGD,
    YTELSE_FRA_OFFENTLIGE
}
