package no.nav.helse.sparkel.inntekt

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.serialization.jackson.JacksonConverter
import io.mockk.every
import io.mockk.mockk
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InntektRestClientTest {

    private val responsMock = mockk<Mock>(relaxed = true)

    @Test
    fun `person uten inntektshistorikk`() = runBlocking {
        every { responsMock.get() } returns tomRespons()
        val inntektsliste =
            inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")

        assertNotNull(inntektsliste)
        assertEquals(0, inntektsliste.size)
    }

    @Test
    fun `person med inntektshistorikk fra org`() = runBlocking {
        every { responsMock.get() } returns responsMedInntekt("orgnummer1", "ORGANISASJON")
        val inntektsliste =
            inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
        assertNotNull(inntektsliste)
        assertEquals(1, inntektsliste.size)
        assertEquals("orgnummer1", inntektsliste.first().inntektsliste.first().orgnummer)
    }

    @Test
    fun `person med inntektshistorikk fra fødselsnummer`() = runBlocking {
        every { responsMock.get() } returns responsMedInntekt("fødselsnummer1", "NATURLIG_IDENT")
        val inntektsliste =
            inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
        assertNotNull(inntektsliste)
        assertEquals(1, inntektsliste.size)
        assertEquals("fødselsnummer1", inntektsliste.first().inntektsliste.first().fødselsnummer)
    }

    @Test
    fun `person med inntektshistorikk fra aktørId`() {
        every { responsMock.get() } returns responsMedInntekt("aktørid1", "AKTOER_ID")
        assertThrows<IllegalStateException> { runBlocking { inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId") } }
    }

    @Test
    fun `Kaster exception dersom vi får 5xx fra server`() {
        runBlocking {
            every { responsMock.get() } returns "{}"
            every { responsMock.status() } returns HttpStatusCode.InternalServerError
            assertThrows<ServerResponseException> {
                inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
            }
        }
    }

    @Test
    fun `Kaster exception dersom vi får 4xx fra server`() {
        runBlocking {
            every { responsMock.get() } returns "{}"
            every { responsMock.status() } returns HttpStatusCode.NotFound
            assertThrows<ClientRequestException> {
                inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
            }
        }
    }

    @Test
    fun `Kaster exception dersom vi får 3xx fra server`() {
        runBlocking {
            every { responsMock.get() } returns "{}"
            every { responsMock.status() } returns HttpStatusCode.PermanentRedirect
            assertThrows<RedirectResponseException> {
                inntektRestClient.hentInntektsliste("fnr", YearMonth.of(2019, 1), YearMonth.of(2019, 10), "8-30", "callId")
            }
        }
    }

    private val inntektRestClient = InntektRestClient(
        "http://localhost.no",
        "resourceId",
        HttpClient(MockEngine) {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }
            engine {
                addHandler { request ->
                    if (request.url.fullPath.startsWith("/api/v1/hentinntektliste")) {
                        respond(content = responsMock.get(), status = responsMock.status())
                    } else {
                        error("Endepunktet finnes ikke ${request.url.fullPath}")
                    }
                }
            }
        },
        tokenSupplier = { "token" }
    )
}

private fun tomRespons() =
    """{
        "ident": {
            "identifikator": "fnr",
            "aktoerType": "NATURLIG_IDENT"
        }
    }"""

private fun responsMedInntekt(identifikator: String, aktoerType: String) =
    """
        {"arbeidsInntektMaaned": [
                    {
                        "aarMaaned": "2018-12",
                        "arbeidsInntektInformasjon": {
                            "inntektListe": [
                                {
                                    "inntektType": "LOENNSINNTEKT",
                                    "beloep": 25000,
                                    "fordel": "kontantytelse",
                                    "inntektskilde": "A-ordningen",
                                    "inntektsperiodetype": "Maaned",
                                    "inntektsstatus": "LoependeInnrapportert",
                                    "leveringstidspunkt": "2020-01",
                                    "utbetaltIMaaned": "2018-12",
                                    "opplysningspliktig": {
                                        "identifikator": "orgnummer1",
                                        "aktoerType": "ORGANISASJON"
                                    },
                                    "virksomhet": {
                                        "identifikator": "$identifikator",
                                        "aktoerType": "$aktoerType"
                                    },
                                    "inntektsmottaker": {
                                        "identifikator": "aktørId",
                                        "aktoerType": "NATURLIG_IDENT"
                                    },
                                    "inngaarIGrunnlagForTrekk": true,
                                    "utloeserArbeidsgiveravgift": true,
                                    "informasjonsstatus": "InngaarAlltid",
                                    "beskrivelse": "fastloenn"
                                }
                            ]
                        }
                    }
                ]}
"""

private class Mock {
    fun status(): HttpStatusCode = HttpStatusCode.OK
    fun get() = "{}"
}
