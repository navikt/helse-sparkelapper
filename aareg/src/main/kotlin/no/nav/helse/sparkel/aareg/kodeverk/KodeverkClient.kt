package no.nav.helse.sparkel.aareg.kodeverk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.encodeURLPath
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.sikkerlogg
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("sparkel-aareg")

class KodeverkClient(
    private val kodeverkBaseUrl: String,
    private val appName: String
) {
    private fun ObjectMapper.readTreeLogError(response: String) = try {
        readTree(response)
    } catch (throwable: Throwable) {
        log.error("Klarte ikke å parse response fra Kodeverk som JSON\n$response", throwable)
        throw throwable
    }

    private val næringer: JsonNode by lazy {
        objectMapper.readTreeLogError(hentFraKodeverk("/v1/kodeverk/Næringskoder/koder/betydninger".encodeURLPath()))
    }
    private val yrker: JsonNode by lazy {
        objectMapper.readTreeLogError(hentFraKodeverk("/v1/kodeverk/Yrker/koder/betydninger"))
    }

    fun getNæring(kode: String): String {
        return requireNotNull(næringer.hentTekst(kode))
    }

    fun getYrke(kode: String): String {
        return requireNotNull(yrker.hentTekst(kode))
    }

    private fun hentFraKodeverk(path: String): String {
        val (responseCode, body) = URL("$kodeverkBaseUrl$path?spraak=nb&ekskluderUgyldige=true&oppslagsdato=${LocalDate.now()}").get(
            "Nav-Call-Id" to "${UUID.randomUUID()}",
            "Nav-Consumer-Id" to appName
        )
        log.info("Kodeverk status $responseCode for path $path")
        return body
    }

    private companion object {
        private fun URL.get(
            vararg headers: Pair<String, String>
        ) = with(openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            doOutput = true
            headers.forEach { (key, value) ->
                setRequestProperty(key, value)
            }
            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            val responseBody = stream?.use { it.bufferedReader().readText() }
            if (responseBody == null || responseCode >= 300) {
                sikkerlogg.error("Mottok responseCode=$responseCode, url=$url:\nBody:\n$responseBody")
                throw IllegalStateException("Mottok responseCode=$responseCode, url=$url")
            }
            responseCode to responseBody
        }
    }
}

fun JsonNode.hentTekst(kode: String): String =
    path("betydninger").path(kode)
        .takeIf { !it.isMissingNode }
        ?.first()
        ?.path("beskrivelser")?.path("nb")?.path("tekst")?.asText()
        ?: let {
            log.warn("Mangler betydning for næringskode $kode")
            "Ukjent"
        }
