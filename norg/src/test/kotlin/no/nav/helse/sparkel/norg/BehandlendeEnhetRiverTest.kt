package no.nav.helse.sparkel.norg

import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.jackson.jackson
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BehandlendeEnhetRiverTest {

    private val NAV_GØVIK = "100000090"

    @Test
    fun `happy case`() {
        coEvery { pdlMock.finnGeografiskTilhørighet(any(), any()) }.returns(
            GeografiskTilknytning(null, "3407", null)
        )
        rapid.sendTestMessage(behov)
        assertEquals(NAV_GØVIK, rapid.inspektør.message(0)["@løsning"]["HentEnhet"].textValue())
    }

    @Test
    fun `404 fra norg`() {
        coEvery { pdlMock.finnGeografiskTilhørighet(any(), any()) }.returns(
            GeografiskTilknytning("SWE", null, null)
        )
        rapid.sendTestMessage(behov)
        assertEquals(NAV_OPPFOLGING_UTLAND_KONTOR_NR, rapid.inspektør.message(0)["@løsning"]["HentEnhet"].textValue())
    }

    @Test
    fun `500 fra norg`() {
        coEvery { pdlMock.finnGeografiskTilhørighet(any(), any()) }.returns(
            GeografiskTilknytning("FOO", null, null)
        )
        rapid.sendTestMessage(behov)
        assertEquals(0, rapid.inspektør.size)
    }

    @Language("JSON")
    val behov = """
        {
          "@event_name": "behov",
          "@behov": [
            "HentEnhet"
          ],
          "@id": "2dad52b1-f58e-4c26-bb24-970705cdea67",
          "@opprettet": "2020-05-05T11:16:12.678539",
          "hendelseId": "c2d3ce2e-abeb-4c27-a7d3-e45f23ef26f7",
          "fødselsnummer": "12345",
          "orgnummer": "89123"
        }
"""

    private val pdlMock = mockk<PDL> {
        coEvery { finnAdressebeskyttelse(any(), any()) } returns Adressebeskyttelse.FORTROLIG
    }

    private val client = HttpClient(MockEngine) {
        install(ContentNegotiation) {
            jackson()
        }
        expectSuccess = false
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/baseurl/enhet/navkontor/SWE" -> {
                        respond(
                            "{\"field\":null,\"message\":\"Enheten med nummeret ''{0}'' eksisterer ikke\"}",
                            status = HttpStatusCode.NotFound,
                            headersOf(name = HttpHeaders.ContentType, value = "application/json")
                        )
                    }
                    "/baseurl/enhet/navkontor/3407" -> {
                        respond(
                            "{\"enhetNr\": $NAV_GØVIK }",
                            status = HttpStatusCode.OK,
                            headersOf(name = HttpHeaders.ContentType, value = "application/json")
                        )
                    }
                    else -> respond(
                        "{}",
                        status = HttpStatusCode.InternalServerError,
                        headersOf(name = HttpHeaders.ContentType, value = "application/json")
                    )
                }
            }
        }
    }

    private val azureTokenMock = object : AzureTokenProvider {
        override fun bearerToken(scope: String): AzureToken {
            return AzureToken("mittToken", LocalDateTime.MAX)
        }

        override fun onBehalfOfToken(scope: String, token: String): AzureToken {
            throw NotImplementedError("ikke implementert i mock")
        }
    }
    private val norg2Client = Norg2Client("baseurl", "norg2-scope", azureTokenMock, client)
    private val rapid = TestRapid()
        .apply {
            BehandlendeEnhetRiver(this, PersoninfoService(norg2Client, pdlMock))
        }
}

