package no.nav.helse.sparkel.aareg.kodeverk

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.encodeURLPath
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.azureTokenStub
import no.nav.helse.sparkel.aareg.objectMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private const val kodeverkverdi = "Engroshandel med innsatsvarer ikke nevnt annet sted"
private const val kodeverkRef = "46.769"
private const val yrkeverdi = "Gaming, men for folk som er boomers"
private const val yrkeRef = "1337"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KodeverkClientTest {
    private lateinit var server: WireMockServer

    @BeforeAll
    fun beforeAll() {
        server = WireMockServer(WireMockConfiguration.options().dynamicPort())
        server.start()
        WireMock.configureFor(server.port())
        mock("/api/v1/kodeverk/Næringskoder/koder/betydninger", næringRespons)
        mock("/api/v1/kodeverk/Yrker/koder/betydninger", yrkeRespons)
    }

    @AfterAll
    fun afterAll() = server.stop()

    @Test
    fun `henter tekst fra kodeverksrespons`() {
        assertEquals(kodeverkverdi, requireNotNull(objectMapper.readTree(næringRespons)).hentTekst(kodeverkRef))
    }

    @Test
    fun `henter næring`() {
        val kodeverkClient = KodeverkClient(
            kodeverkBaseUrl = server.baseUrl(),
            appName = "sparkel-aareg",
            kodeverkOauthScope = "kodeverk-oauth-scope",
            azureTokenProvider = azureTokenStub()
        )

        assertEquals(kodeverkverdi, kodeverkClient.getNæring(kodeverkRef))
    }

    @Test
    fun `henter yrke`() {
        val kodeverkClient = KodeverkClient(
            kodeverkBaseUrl = server.baseUrl(),
            appName = "sparkel-aareg",
            kodeverkOauthScope = "kodeverk-oauth-scope",
            azureTokenProvider = azureTokenStub()
        )

        assertEquals(yrkeverdi, kodeverkClient.getYrke(yrkeRef))
    }

    @Test
    fun `cacher responsen`() {
        val kodeverkClient = KodeverkClient(
            kodeverkBaseUrl = server.baseUrl(),
            appName = "sparkel-aareg",
            kodeverkOauthScope = "kodeverk-oauth-scope",
            azureTokenProvider = azureTokenStub()
        )

        assertEquals(kodeverkverdi, kodeverkClient.getNæring(kodeverkRef))
        mock("/api/v1/kodeverk/N%c3%a6ringskoder/koder/betydninger", "TEXT/PLAIN", 503)
        assertEquals(kodeverkverdi, kodeverkClient.getNæring(kodeverkRef))
    }

    private fun mock(path: String, response: String, status: Int = 200) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching("$path.*".encodeURLPath()))
                .withHeader("Nav-Consumer-Id", WireMock.equalTo("sparkel-aareg"))
                .withHeader("Nav-Call-Id", AnythingPattern())
                .withQueryParam("spraak", WireMock.equalTo("nb"))
                .withQueryParam("ekskluderUgyldige", WireMock.equalTo("true"))
                .withQueryParam("oppslagsdato", WireMock.matching("\\d{4}-\\d{2}-\\d{2}"))
                .willReturn(WireMock.aResponse().withStatus(status).withBody(response))
        )
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
