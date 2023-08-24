package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
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
    suspend fun hentFraAaregV1(
        fnr: String,
        callId: UUID
    ): ArrayNode {
        val response = hentV1(fnr, callId, "$baseUrl/v1/arbeidstaker/arbeidsforhold?arbeidsforholdtype=ordinaertArbeidsforhold,maritimtArbeidsforhold,forenkletOppgjoersordning")

        sikkerlogg.info("AaregResponse status: ${response.status}\n${response.bodyAsText()}")
        val responseValue = objectMapper.readTree(response.bodyAsText())
        if (!responseValue.isArray) throw AaregException(
            responseValue.path("melding").asText("Ukjent respons fra Aareg"), responseValue
        )
        return responseValue as ArrayNode
    }

    suspend fun hentFraAareg(
        fnr: String,
        callId: UUID
    ): List<AaregArbeidsforhold> {
        val response = hent(
            fnr,
            callId,
            "$baseUrl/v2/arbeidstaker/arbeidsforhold?sporingsinformasjon=false&arbeidsforholdstatus=AKTIV,FREMTIDIG,AVSLUTTET"
        )

        sikkerlogg.info("V2-response:\n${response.bodyAsText()}")

        return response.body()
    }

    private suspend fun hentV1(fnr: String, callId: UUID, url: String) =
        httpClient.prepareGet(url) {
            header("Authorization", "Bearer ${tokenSupplier()}")
            System.getenv("NAIS_APP_NAME")?.also { header("Nav-Consumer-Id", it) }
            header("Nav-Call-Id", callId)
            accept(ContentType.Application.Json)
            header("Nav-Personident", fnr)
        }.execute()

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
) {
    enum class Arbeidsforholdtype {
        FORENKLET_OPPGJØRSORDNING,
        FRILANSER,
        MARITIMT,
        ORDINÆRT;

        companion object {
            fun fraAareg(type: String) = when (type) {
                "forenkletOppgjoersordning" -> FORENKLET_OPPGJØRSORDNING
                "frilanserOppdragstakerHonorarPersonerMm" -> FRILANSER
                "maritimtArbeidsforhold" -> MARITIMT
                "ordinaertArbeidsforhold" -> ORDINÆRT
                else -> error("har ikke mappingregel for arbeidsforholdtype: $type")
            }
        }
    }
}

internal fun JsonNode.asOptionalLocalDate() =
    takeIf(JsonNode::isTextual)?.asText()?.takeIf(String::isNotEmpty)?.let { LocalDate.parse(it.substring(0, 10)) }

internal fun JsonNode.asLocalDate() =
    asText().substring(0, 10).let { LocalDate.parse(it) }
