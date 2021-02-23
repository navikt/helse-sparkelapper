package no.nav.helse.sparkel.aareg.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import no.nav.helse.sparkel.aareg.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val kodeverkverdi = "Engroshandel med innsatsvarer ikke nevnt annet sted"
private const val kodeverkRef = "46.769"

internal class KodeverkClientTest {

    @Test
    fun `henter tekst fra kodeverksrespons`() {
        assertEquals(kodeverkverdi, requireNotNull(objectMapper.readTree(næringRespons)).hentTekst(kodeverkRef))
    }

    @Test
    fun `henter næring`() {
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/api/v1/kodeverk/N%C3%A6ringskoder/koder/betydninger" -> respond(næringRespons)
                        else -> respondBadRequest()
                    }
                }
            }
        }
        val kodeverkClient = KodeverkClient(
            httpClient = httpClient,
            kodeverkBaseUrl = "http://base.url",
            appName = "sparkel-aareg"
        )

        kodeverkClient.getNæring(kodeverkRef)
    }

    private val næringRespons = """
            {
              "betydninger": {
                "$kodeverkRef": [
                  {
                    "gyldigFra": "1995-01-01",
                    "gyldigTil": "9999-12-31",
                    "beskrivelser": {
                      "nb": {
                        "term": "$kodeverkverdi",
                        "tekst": "$kodeverkverdi"
                      }
                    }
                  }
                ]
              }
            }
        """
}
