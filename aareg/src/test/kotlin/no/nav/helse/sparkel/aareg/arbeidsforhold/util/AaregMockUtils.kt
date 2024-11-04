package no.nav.helse.sparkel.aareg.arbeidsforhold.util

import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.JacksonConverter
import java.time.LocalDateTime
import no.nav.helse.sparkel.aareg.objectMapper
import org.intellij.lang.annotations.Language

fun azureTokenStub() = object : AzureTokenProvider {
    override fun bearerToken(scope: String) = com.github.navikt.tbd_libs.result_object.Result.Ok(AzureToken("superToken", LocalDateTime.MAX))
    override fun onBehalfOfToken(scope: String, token: String): com.github.navikt.tbd_libs.result_object.Result<AzureToken> =
        throw NotImplementedError("Ikke implementert i mock")
}

fun aaregMockClient(aaregResponse: String = defaultArbeidsforholdResponse(), status: HttpStatusCode = OK) = HttpClient(MockEngine) {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
    expectSuccess = false
    engine {
        addHandler { request ->
            when {
                request.url.fullPath.startsWith("/api/v2/arbeidstaker/arbeidsforhold") -> respond(
                    content = aaregResponse,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )

                else -> error("Endepunktet finnes ikke: ${request.url.fullPath}")
            }
        }
    }
}

@Language("JSON")
fun defaultArbeidsforholdResponse() = """[
    {
        "id": "1",
        "type": {
            "kode": "ordinaertArbeidsforhold",
            "beskrivelse": "Ordinært arbeidsforhold"
        },
        "arbeidstaker": {
            "identer": [
                {
                    "type": "AKTORID",
                    "ident": "9123456789",
                    "gjeldende": true
                },
                {
                    "type": "FOLKEREGISTERIDENT",
                    "ident": "9123456789",
                    "gjeldende": true
                }
            ]
        },
        "arbeidssted": {
            "type": "Underenhet",
            "identer": [
                {
                    "type": "ORGANISASJONSNUMMER",
                    "ident": "123456789"
                }
            ]
        },
        "opplysningspliktig": {
            "type": "Hovedenhet",
            "identer": [
                {
                    "type": "ORGANISASJONSNUMMER",
                    "ident": "912345678"
                }
            ]
        },
        "ansettelsesperiode": {
            "startdato": "2003-08-03",
            "sluttdato": "2010-08-03",
            "sluttaarsak": {
                "kode": "arbeidstakerHarSagtOppSelv",
                "beskrivelse": "Arbeidstaker har sagt opp selv"
            }
        },
        "ansettelsesdetaljer": [
            {
                "type": "Ordinaer",
                "arbeidstidsordning": {
                    "kode": "doegnkontinuerligSkiftOgTurnus355",
                    "beskrivelse": "Døgnkontinuerlig skiftarbeid og turnusarbeid (35,5 t/u)"
                },
                "ansettelsesform": {
                    "kode": "midlertidig",
                    "beskrivelse": "Midlertidig ansettelse"
                },
                "yrke": {
                    "kode": "5132121",
                    "beskrivelse": "HELSEFAGARBEIDER"
                },
                "antallTimerPrUke": 35.5,
                "avtaltStillingsprosent": 100.0,
                "sisteStillingsprosentendring": "2021-01-01",
                "sisteLoennsendring": "2022-05-01",
                "rapporteringsmaaneder": {
                    "fra": "2022-09",
                    "til": null
                }
            }
        ],
        "rapporteringsordning": {
            "kode": "A_ORDNINGEN",
            "beskrivelse": "Rapportert via a-ordningen (2015-d.d.)"
        },
        "navArbeidsforholdId": 12345678,
        "navVersjon": 93,
        "navUuid": "a12345e6-c7f1-4949-6737-25e1cac0b1ae",
        "opprettet": "2020-11-05T17:30:47.593",
        "sistBekreftet": "2023-03-02T14:00:19",
        "sistEndret": "2022-10-05T20:33:49",
        "bruksperiode": {
            "fom": "2020-11-05T17:35:26.103",
            "tom": null
        }
    },
    {
        "type": {
            "kode": "forenkletOppgjoersordning",
            "beskrivelse": "Forenklet oppgjørsordning"
        },
        "arbeidstaker": {
            "identer": [
                {
                    "type": "AKTORID",
                    "ident": "9123456789",
                    "gjeldende": true
                },
                {
                    "type": "FOLKEREGISTERIDENT",
                    "ident": "9123456789",
                    "gjeldende": true
                }
            ]
        },
        "arbeidssted": {
            "type": "Person",
            "identer": [
                {
                    "type": "AKTORID",
                    "ident": "9123456789",
                    "gjeldende": true
                },
                {
                    "type": "FOLKEREGISTERIDENT",
                    "ident": "9123456789",
                    "gjeldende": true
                }
            ]
        },
        "opplysningspliktig": {
            "type": "Person",
            "identer": [
                {
                    "type": "AKTORID",
                    "ident": "9123456789",
                    "gjeldende": true
                },
                {
                    "type": "FOLKEREGISTERIDENT",
                    "ident": "9123456789",
                    "gjeldende": true
                }
            ]
        },
        "ansettelsesperiode": {
            "startdato": "2017-05-01",
            "sluttdato": "2017-11-30"
        },
        "ansettelsesdetaljer": [
            {
                "type": "Forenklet",
                "yrke": {
                    "kode": "5136102",
                    "beskrivelse": "DAGMAMMA"
                },
                "rapporteringsmaaneder": {
                    "fra": "2018-06",
                    "til": null
                }
            }
        ],
        "rapporteringsordning": {
            "kode": "A_ORDNINGEN",
            "beskrivelse": "Rapportert via a-ordningen (2015-d.d.)"
        },
        "navArbeidsforholdId": 61234509,
        "navVersjon": 0,
        "navUuid": "c358d127-6a2d-4f1c-9c27-5c4126d4aaaa",
        "opprettet": "2018-06-26T12:34:36.66",
        "sistBekreftet": "2018-06-26T12:34:36",
        "sistEndret": "2018-06-26T12:45:29",
        "bruksperiode": {
            "fom": "2018-06-26T12:45:09.85",
            "tom": null
        }
    }
]
"""
