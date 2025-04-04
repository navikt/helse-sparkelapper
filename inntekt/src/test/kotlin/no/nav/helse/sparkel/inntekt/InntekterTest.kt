package no.nav.helse.sparkel.inntekt

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.navikt.tbd_libs.rapids_and_rivers.asYearMonth
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.jackson.JacksonConverter
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class InntekterTest {
    private val testRapid = TestRapid()
    private val inntektRestClient = InntektRestClient("http://base.url", "", HttpClient(MockEngine) {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }
        engine {
            addHandler { request ->
                if (request.url.fullPath.startsWith("/api/v1/hentinntektliste") && request.body.toByteArray()
                        .getFilter() == Inntekter.Type.InntekterForSykepengegrunnlag.ainntektfilter
                ) {
                    respond(sykepengegrunnlagResponse())
                }  else if (request.url.fullPath.startsWith("/api/v1/hentinntektliste") && request.body.toByteArray()
                        .getFilter() == Inntekter.Type.InntekterForOpptjeningsvurdering.ainntektfilter
                    && request.body.toByteArray().getAntallMåneder() == 1
                ) {
                    respond(opptjeningsvurderingResponse())
                } else if (request.url.fullPath.startsWith("/api/v1/hentinntektliste") && request.body.toByteArray()
                        .getFilter() == Inntekter.Type.InntekterForSammenligningsgrunnlag.ainntektfilter
                ) {
                    respond(sammenligningsgrunnlagResponse())
                } else {
                    respondError(HttpStatusCode.InternalServerError)
                }
            }
        }
    }, tokenSupplier = { "token" })

    private fun ByteArray.getFilter() = objectMapper.readTree(this).get("ainntektsfilter").asText()

    private fun ByteArray.getAntallMåneder(): Int {
        val månedFom = objectMapper.readTree(this).get("maanedFom").asYearMonth()
        val månedTom = objectMapper.readTree(this).get("maanedTom").asYearMonth()
        if (månedFom == månedTom) return 1
        return ChronoUnit.MONTHS.between(månedFom, månedTom).toInt() + 1
    }

    init {
        Inntekter(testRapid, inntektRestClient)
    }

    @BeforeEach
    fun beforeEach() {
        testRapid.reset()
    }

    @Test
    fun `Inntekter for opptjeningsvurdering`() {
        val start = YearMonth.of(2024, 7)
        val slutt = YearMonth.of(2024, 7)
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForOpptjeningsvurdering))
        assertEquals(1, testRapid.inspektør.size)
        assertLøsning(Inntekter.Type.InntekterForOpptjeningsvurdering, YearMonth.of(2024, 7))
    }

    @Test
    fun `Inntekter for sykepengegrunnlag`() {
        val start = YearMonth.of(2020, 2)
        val slutt = YearMonth.of(2021, 1)
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForSykepengegrunnlag))
        assertEquals(1, testRapid.inspektør.size)
        assertLøsning(Inntekter.Type.InntekterForSykepengegrunnlag, YearMonth.of(2019, 1), YearMonth.of(2019, 2), YearMonth.of(2019, 3))
    }

    @Test
    fun `Inntekter for sykepengegrunnlag for arbeidsgiver`() {
        val start = YearMonth.of(2020, 2)
        val slutt = YearMonth.of(2021, 1)
        val orgnr = "a3"
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForSykepengegrunnlagForArbeidsgiver, orgnummer = orgnr))
        assertEquals(1, testRapid.inspektør.size)
        assertLøsning(Inntekter.Type.InntekterForSykepengegrunnlagForArbeidsgiver, YearMonth.of(2019, 1), YearMonth.of(2019, 2), YearMonth.of(2019, 3))
    }

    @Test
    fun `Inntekter for sammenligningsgrunnlag`() {
        val start = YearMonth.of(2020, 2)
        val slutt = YearMonth.of(2021, 1)
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForSammenligningsgrunnlag))
        assertEquals(1, testRapid.inspektør.size)
        assertLøsning(Inntekter.Type.InntekterForSammenligningsgrunnlag, YearMonth.of(2020, 1), YearMonth.of(2020, 2), YearMonth.of(2020, 3))
    }

    @Test
    fun `mapper nye felter beskrivelse og fordel sammenligningsgrunnlag`() {
        val start = YearMonth.of(2020, 2)
        val slutt = YearMonth.of(2021, 1)
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForSammenligningsgrunnlag))
        val inntekt0 = testRapid.inspektør.message(0).path("@løsning")
            .path(Inntekter.Type.InntekterForSammenligningsgrunnlag.name)[0]
        val inntekt1 = testRapid.inspektør.message(0).path("@løsning")
            .path(Inntekter.Type.InntekterForSammenligningsgrunnlag.name)[1]
        assertEquals(0, inntekt0["inntektsliste"].size())
        assertEquals(1, inntekt1["inntektsliste"].size())
        inntekt1["inntektsliste"].forEach {
            assertEquals("fastloenn", it.path("beskrivelse").textValue())
            assertEquals("kontantytelse", it.path("fordel").textValue())
        }
    }

    @Test
    fun `mapper nye felter beskrivelse og fordel sykepengegrunnlag`() {
        val start = YearMonth.of(2020, 2)
        val slutt = YearMonth.of(2021, 1)
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForSykepengegrunnlag))
        val inntekt0 =
            testRapid.inspektør.message(0).path("@løsning").path(Inntekter.Type.InntekterForSykepengegrunnlag.name)[0]
        val inntekt1 =
            testRapid.inspektør.message(0).path("@løsning").path(Inntekter.Type.InntekterForSykepengegrunnlag.name)[1]
        assertEquals(0, inntekt0["inntektsliste"].size())
        assertEquals(1, inntekt1["inntektsliste"].size())
        inntekt1["inntektsliste"].forEach {
            assertEquals("fastloenn", it.path("beskrivelse").textValue())
            assertEquals("kontantytelse", it.path("fordel").textValue())
        }
    }

    @ParameterizedTest
    @EnumSource(Inntekter.Type::class, names = ["InntekterForSykepengegrunnlagForArbeidsgiver"], mode = EnumSource.Mode.EXCLUDE)
    fun `ignorerer for gamle behov`() {
        val behov = objectMapper.readTree(behov(
            YearMonth.of(2020, 2),
            YearMonth.of(2021, 1),
            Inntekter.Type.InntekterForSammenligningsgrunnlag
        )).apply {
            (this as ObjectNode).put("@opprettet", LocalDateTime.now().minusMinutes(31).toString())
        }
        testRapid.sendTestMessage(behov.toString())
        assertEquals(0, testRapid.inspektør.size)
    }

    private fun assertLøsning(behovType: Inntekter.Type, vararg yearsMonths: YearMonth) {
        assertTrue(testRapid.inspektør.message(0).hasNonNull("@løsning"))
        assertEquals(yearsMonths.toList(),
            testRapid.inspektør.message(0).path("@løsning").path(behovType.name)
                .map { it.path("årMåned").asYearMonth() }
        )
    }

    private fun behov(start: YearMonth, slutt: YearMonth, type: Inntekter.Type, id: UUID = UUID.randomUUID(), orgnummer: String? = null) =
        objectMapper.writeValueAsString(behovMap(start, slutt, id, type, orgnummer))

    private fun behovMap(start: YearMonth, slutt: YearMonth, id: UUID, type: Inntekter.Type, orgnummer: String? = null): Map<String, Any> {
        val detaljer = mutableMapOf(
            "beregningStart" to "$start",
            "beregningSlutt" to "$slutt"
        ).apply {
            compute("organisasjonsnummer") { _, _ -> orgnummer }
        }
        return mapOf(
            "@id" to id,
            "@behov" to listOf(type.name),
            "fødselsnummer" to "123",
            "vedtaksperiodeId" to "vedtaksperiodeId",
            type.name to detaljer
        )
    }

    fun sammenligningsgrunnlagResponse() = """
    {
        "arbeidsInntektMaaned": [
            {
                "aarMaaned": "2020-01",
                "arbeidsInntektInformasjon": null
            },
            {
                "aarMaaned": "2020-02",
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
                                "identifikator": "fnr",
                                "aktoerType": "NATURLIG_IDENT"
                            },
                            "inngaarIGrunnlagForTrekk": true,
                            "utloeserArbeidsgiveravgift": true,
                            "informasjonsstatus": "InngaarAlltid",
                            "beskrivelse": "fastloenn"
                        }
                    ]
                }
            },
            {
                "aarMaaned": "2020-03",
                "arbeidsInntektInformasjon": {
                    "arbeidsforholdListe": [
                        {
                            "antallTimerPerUkeSomEnFullStillingTilsvarer": 37.5,
                            "arbeidstidsordning": "ikkeSkift",
                            "frilansPeriodeFom": "2018-09-24",
                            "stillingsprosent": 0.0,
                            "yrke": "2221110",
                            "arbeidsforholdID": "0001-0001-0001-1",
                            "arbeidsforholdstype": "frilanserOppdragstakerHonorarPersonerMm",
                            "arbeidsgiver": {
                              "identifikator": "orgnummer2",
                              "aktoerType": "ORGANISASJON"
                            },
                            "arbeidstaker": {
                              "identifikator": "20046913337",
                              "aktoerType": "NATURLIG_IDENT"
                            }
                      }
                    ]
                }
            }
        ],
        "ident": {
            "identifikator": "fnr",
            "aktoerType": "NATURLIG_IDENT"
        }
    }
"""
    fun opptjeningsvurderingResponse() = """
    {
        "arbeidsInntektMaaned": [
            {
                "aarMaaned": "2024-07",
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
                                "identifikator": "fnr",
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
        ],
        "ident": {
            "identifikator": "fnr",
            "aktoerType": "NATURLIG_IDENT"
        }
    }
"""

fun sykepengegrunnlagResponse() = """
    {
        "arbeidsInntektMaaned": [
            {
                "aarMaaned": "2019-01",
                "arbeidsInntektInformasjon": null
            },
            {
                "aarMaaned": "2019-02",
                "arbeidsInntektInformasjon": {
                    "arbeidsforholdListe": [
                        {
                            "antallTimerPerUkeSomEnFullStillingTilsvarer": 37.5,
                            "arbeidstidsordning": "ikkeSkift",
                            "frilansPeriodeFom": "2018-09-24",
                            "stillingsprosent": 0.0,
                            "yrke": "2221110",
                            "arbeidsforholdID": "0001-0001-0001-1",
                            "arbeidsforholdstype": "frilanserOppdragstakerHonorarPersonerMm",
                            "arbeidsgiver": {
                              "identifikator": "orgnummer2",
                              "aktoerType": "ORGANISASJON"
                            },
                            "arbeidstaker": {
                              "identifikator": "20046913337",
                              "aktoerType": "NATURLIG_IDENT"
                            }
                      }
                    ],
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
                                "identifikator": "fnr",
                                "aktoerType": "NATURLIG_IDENT"
                            },
                            "inngaarIGrunnlagForTrekk": true,
                            "utloeserArbeidsgiveravgift": true,
                            "informasjonsstatus": "InngaarAlltid",
                            "beskrivelse": "fastloenn"
                        }
                    ]
                }
            },
            {
                "aarMaaned": "2019-03",
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
                                "identifikator": "fnr",
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
        ],
        "ident": {
            "identifikator": "fnr",
            "aktoerType": "NATURLIG_IDENT"
        }
    }
"""

}
