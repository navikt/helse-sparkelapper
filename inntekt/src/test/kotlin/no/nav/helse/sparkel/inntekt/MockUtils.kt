package no.nav.helse.sparkel.inntekt

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.time.ZoneOffset

interface ResponseGenerator {
    fun hentInntekter() = inntekterEmptyResponse()
}

internal fun defaultMockResponseGenerator() = mockk<ResponseGenerator>(relaxed = true) {
    every { hentInntekter() } returns inntekterResponse()
}

internal fun mockHttpClient(mockResponseGenerator: ResponseGenerator) = HttpClient(MockEngine) {
    install(JsonFeature) {
        serializer = JacksonSerializer(jackson = objectMapper)
    }
    engine {
        addHandler { request ->
            when {
                request.url.fullPath.startsWith("/api/v1/hentinntektliste") -> respond(mockResponseGenerator.hentInntekter())
                else -> respondError(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun inntekterEmptyResponse() = """[]"""
fun inntekterResponse() = """
    {
        "arbeidsInntektMaaned": [
            {
                "aarMaaned": "2018-12",
                "arbeidsInntektInformasjon": null
            },
            {
                "aarMaaned": "2019-05",
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
                            "utbetaltIMaaned": "2019-05",
                            "opplysningspliktig": {
                                "identifikator": "orgnummer2",
                                "aktoerType": "ORGANISASJON"
                            },
                            "virksomhet": {
                                "identifikator": "orgnummer2",
                                "aktoerType": "ORGANISASJON"
                            },
                            "inntektsmottaker": {
                                "identifikator": "aktørId",
                                "aktoerType": "AKTOER_ID"
                            },
                            "inngaarIGrunnlagForTrekk": true,
                            "utloeserArbeidsgiveravgift": true,
                            "informasjonsstatus": "InngaarAlltid",
                            "beskrivelse": "fastloenn"
                        }
                    ]
                }
            }
        ],
        "ident": {
            "identifikator": "aktørId",
            "aktoerType": "AKTOER_ID"
        }
    }
"""

internal val mockStsRestClient = StsRestClient(
    baseUrl = "",
    serviceUser = ServiceUser("yes", "yes"),
    httpClient = HttpClient(MockEngine) {
        engine {
            addHandler {
                val tokenExpirationTime = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                respond("""{"access_token":"token", "expires_in":$tokenExpirationTime, "token_type":"yes"}""")
            }
        }
    })
