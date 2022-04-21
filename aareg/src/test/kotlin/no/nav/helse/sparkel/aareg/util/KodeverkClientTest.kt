package no.nav.helse.sparkel.aareg.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import no.nav.helse.sparkel.aareg.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

private const val kodeverkverdi = "Engroshandel med innsatsvarer ikke nevnt annet sted"
private const val kodeverkRef = "46.769"
private const val yrkeverdi = "Gaming, men for folk som er boomers"
private const val yrkeRef = "1337"

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
                        "/api/v1/kodeverk/Næringskoder/koder/betydninger" -> respond(næringRespons)
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

        assertEquals(kodeverkverdi, kodeverkClient.getNæring(kodeverkRef))
    }

    @Test
    fun `henter yrke`() {
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/api/v1/kodeverk/Yrker/koder/betydninger" -> respond(yrkeRespons)
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

        assertEquals(yrkeverdi, kodeverkClient.getYrke(yrkeRef))
    }

    @Test
    fun `cacher responsen`() {
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/api/v1/kodeverk/Næringskoder/koder/betydninger" -> respond(næringRespons)
                        else -> respondBadRequest()
                    }
                }
                addHandler {
                    fail("skulle ha brukt cachet verdi")
                }
            }
        }
        val kodeverkClient = KodeverkClient(
            httpClient = httpClient,
            kodeverkBaseUrl = "http://base.url",
            appName = "sparkel-aareg"
        )

        assertEquals(kodeverkverdi, kodeverkClient.getNæring(kodeverkRef))
        assertEquals(kodeverkverdi, kodeverkClient.getNæring(kodeverkRef))
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

    private val yrkeRespons = """
            {
              "betydninger": {
                "$yrkeRef": [
                  {
                    "gyldigFra": "1995-01-01",
                    "gyldigTil": "9999-12-31",
                    "beskrivelser": {
                      "nb": {
                        "term": "$yrkeverdi",
                        "tekst": "$yrkeverdi"
                      }
                    }
                  }
                ]
              }
            }
        """
}
