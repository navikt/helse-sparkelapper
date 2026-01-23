package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
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
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.sikkerlogg
import no.nav.helse.sparkel.retry

class AaregClient(
    private val baseUrl: String,
    private val scope: String,
    private val tokenSupplier: AzureTokenProvider,
    private val httpClient: HttpClient = HttpClient()
) {
    internal suspend inline fun <reified T> hentFraAareg(
        fnr: String,
        callId: UUID,
    ): List<T> = retry("arbeidsforhold") {
        val response = hent(
            fnr,
            "$baseUrl/api/v2/arbeidstaker/arbeidsforhold?sporingsinformasjon=false&arbeidsforholdstatus=AKTIV,FREMTIDIG,AVSLUTTET"
        )

        val responseBody = response.bodyAsText()
        sikkerlogg.info("AaregResponse: ${response.status}\n$responseBody")

        try {
            response.body()
        } catch (e: JsonConvertException) {
            sikkerlogg.warn("Feil under deserialisering av svar fra Aareg", e)
            val melding = feilmeldingFraAaregEllerGenerellTekst(responseBody)
            throw if (response.status == HttpStatusCode.NotFound && melding == "Ukjent ident") UkjentIdentException()
            else AaregException(melding, responseBody, response.status, e)
        }
    }

    private fun feilmeldingFraAaregEllerGenerellTekst(responseBody: String) = try {
        objectMapper.readValue<AaregMeldingerResponse>(responseBody).meldinger.joinToString()
    } catch (_: Exception) {
        "Klarte ikke å deserialisere svaret fra Aareg"
    }

    private suspend fun hent(fnr: String, url: String) =
        httpClient.get(url) {
            val azureToken = tokenSupplier.bearerToken(scope).getOrThrow()
            header("Authorization", "Bearer ${azureToken.token}")
            System.getenv("NAIS_APP_NAME")?.also { header("Nav-Consumer-Id", it) }
            accept(ContentType.Application.Json)
            header("Nav-Personident", fnr)
        }
}

class AaregException(
    message: String,
    private val responseValue: String,
    private val status: HttpStatusCode,
    cause: Throwable,
) : RuntimeException(message, cause) {
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
