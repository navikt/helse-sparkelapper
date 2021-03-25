package no.nav.helse.sparkel.aareg.util

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.aareg.objectMapper
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.UUID

private val log = LoggerFactory.getLogger("sparkel-aareg")

class KodeverkClient(
    private val httpClient: HttpClient,
    private val kodeverkBaseUrl: String,
    private val appName: String
) {
    private val næringer: JsonNode by lazy {
        objectMapper.readTree(hentFraKodeverk("/api/v1/kodeverk/Næringskoder/koder/betydninger"))
    }
    private val yrker: JsonNode by lazy {
        objectMapper.readTree(hentFraKodeverk("/api/v1/kodeverk/Yrker/koder/betydninger"))
    }

    fun getNæring(kode: String): String = requireNotNull(næringer.hentTekst(kode))

    fun getYrke(kode: String) = requireNotNull(yrker.hentTekst(kode))

    private fun hentFraKodeverk(path: String): String = runBlocking {
        httpClient.get<HttpStatement>("$kodeverkBaseUrl$path") {
            setup(UUID.randomUUID().toString())
        }.receive()
    }

    private fun HttpRequestBuilder.setup(callId: String) {
        header("Nav-Call-Id", callId)
        header("Nav-Consumer-Id", appName)
        parameter("spraak", "nb")
        parameter("ekskluderUgyldige", true)
        parameter("oppslagsdato", LocalDate.now())
    }
}

fun JsonNode.hentTekst(kode: String): String =
    path("betydninger").path(kode).takeIf { !it.isMissingNode }
        ?.first()
        ?.path("beskrivelser")?.path("nb")?.path("tekst")?.asText()
        ?: let {
            log.warn("Mangler betydning for næringskode $kode")
            "Ukjent"
        }

