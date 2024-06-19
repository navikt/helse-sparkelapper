package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.AaregArbeidsforhold
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.sikkerlogg
import no.nav.helse.sparkel.retry

class AaregClient(
    private val baseUrl: String,
    private val scope: String,
    private val tokenSupplier: AzureTokenProvider,
    private val httpClient: HttpClient = HttpClient()
) {
    suspend fun hentFraAareg(
        fnr: String,
        callId: UUID,
    ): List<AaregArbeidsforhold> = retry("arbeidsforhold") {
        val response = hent(
            fnr,
            callId,
            "$baseUrl/api/v2/arbeidstaker/arbeidsforhold?sporingsinformasjon=false&arbeidsforholdstatus=AKTIV,FREMTIDIG,AVSLUTTET"
        )

        val responseBody = response.bodyAsText()
        sikkerlogg.info("AaregResponse: ${response.status}\n$responseBody")

        try {
            response.body<List<AaregArbeidsforhold>>()
        } catch (e: JsonConvertException) {
            val melding = try {
                objectMapper.readValue<AaregMeldingerResponse>(responseBody).meldinger.joinToString()
            } catch (_: Exception) {
                "Ukjent respons fra Aareg"
            }
            throw if (response.status == HttpStatusCode.NotFound && melding == "Ukjent ident") UkjentIdentException()
            else AaregException(melding, responseBody, response.status)
        }
    }

    private suspend fun hent(fnr: String, callId: UUID, url: String) =
        httpClient.get(url) {
            header("Authorization", "Bearer ${tokenSupplier.bearerToken(scope).token}")
            System.getenv("NAIS_APP_NAME")?.also { header("Nav-Consumer-Id", it) }
            header("Nav-Call-Id", callId)
            accept(ContentType.Application.Json)
            header("Nav-Personident", fnr)
        }
}

class AaregException(
    message: String,
    private val responseValue: String,
    private val status: HttpStatusCode
) : RuntimeException(message) {
    fun responseValue() = responseValue
    fun statusFromAareg() = status.value.toString()
}

class UkjentIdentException : RuntimeException()

data class AaregMeldingerResponse(
    val meldinger: List<String>
)

data class Arbeidsforhold(
    val orgnummer: String,
    val ansattSiden: LocalDate,
    val ansattTil: LocalDate?,
    val type: Arbeidsforholdtype
)

data class Orgnummerbytte(
    val byttetFra: ByttetFra,
    val byttetTil: ByttetTil
)

data class ByttetFra(
    val orgnummer: String,
    val sluttDato: LocalDate
)

data class ByttetTil(
    val orgnummer: String,
    val startDato: LocalDate
)

enum class Arbeidsforholdtype {
    FORENKLET_OPPGJØRSORDNING,
    FRILANSER,
    MARITIMT,
    ORDINÆRT,
}

internal fun JsonNode.asOptionalLocalDate() =
    takeIf(JsonNode::isTextual)?.asText()?.takeIf(String::isNotEmpty)?.let { LocalDate.parse(it.substring(0, 10)) }

internal fun JsonNode.asLocalDate() =
    asText().substring(0, 10).let { LocalDate.parse(it) }
