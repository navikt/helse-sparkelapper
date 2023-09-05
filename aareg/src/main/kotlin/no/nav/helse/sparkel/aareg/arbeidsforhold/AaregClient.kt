package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.serialization.JsonConvertException
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.AaregArbeidsforhold
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.sikkerlogg

class AaregClient(
    private val baseUrl: String,
    private val tokenSupplier: () -> String,
    private val httpClient: HttpClient = HttpClient()
) {
    suspend fun hentFraAareg(
        fnr: String,
        callId: UUID
    ): List<AaregArbeidsforhold> {
        val response = hent(
            fnr,
            callId,
            "$baseUrl/v2/arbeidstaker/arbeidsforhold?sporingsinformasjon=false&arbeidsforholdstatus=AKTIV,FREMTIDIG,AVSLUTTET"
        )

        sikkerlogg.info("AaregResponse status:\n${response.bodyAsText()}")

        return try {
            response.body<List<AaregArbeidsforhold>>()
        } catch (e: JsonConvertException) {
            val responseValue = objectMapper.readTree(response.bodyAsText())
            throw AaregException(
                responseValue.path("melding").asText("Ukjent respons fra Aareg"), responseValue
            )
        }
    }

    private suspend fun hent(fnr: String, callId: UUID, url: String) =
        httpClient.get(url) {
            header("Authorization", "Bearer ${tokenSupplier()}")
            System.getenv("NAIS_APP_NAME")?.also { header("Nav-Consumer-Id", it) }
            header("Nav-Call-Id", callId)
            accept(ContentType.Application.Json)
            header("Nav-Personident", fnr)
        }
}

class AaregException(message: String, private val responseValue: JsonNode) : RuntimeException(message) {
    fun responseValue() = responseValue
}

data class Arbeidsforhold(
    val orgnummer: String,
    val ansattSiden: LocalDate,
    val ansattTil: LocalDate?,
    val type: Arbeidsforholdtype
)

enum class Arbeidsforholdtype {
    @JsonProperty("forenkletOppgjoersordning")
    FORENKLET_OPPGJØRSORDNING,
    @JsonProperty("frilanserOppdragstakerHonorarPersonerMm")
    FRILANSER,
    @JsonProperty("maritimtArbeidsforhold")
    MARITIMT,
    @JsonProperty("ordinaertArbeidsforhold")
    ORDINÆRT,
}

internal fun JsonNode.asOptionalLocalDate() =
    takeIf(JsonNode::isTextual)?.asText()?.takeIf(String::isNotEmpty)?.let { LocalDate.parse(it.substring(0, 10)) }

internal fun JsonNode.asLocalDate() =
    asText().substring(0, 10).let { LocalDate.parse(it) }
