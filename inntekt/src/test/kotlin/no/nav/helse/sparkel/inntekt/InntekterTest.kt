package no.nav.helse.sparkel.inntekt

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import no.nav.helse.rapids_rivers.asYearMonth
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.YearMonth
import java.util.UUID

internal class InntekterTest {
    private val testRapid = TestRapid()
    private val inntektRestClient = InntektRestClient("http://base.url", HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = JacksonSerializer(jackson = objectMapper)
        }
        engine {
            addHandler { request ->
                if (request.url.fullPath.startsWith("/api/v1/hentinntektliste") && request.body.toByteArray()
                        .getFilter() == Inntekter.Type.InntekterForSykepengegrunnlag.ainntektfilter
                ) {
                    respond(sykepengegrunnlagResponse())
                } else if (request.url.fullPath.startsWith("/api/v1/hentinntektliste") && request.body.toByteArray()
                        .getFilter() == Inntekter.Type.InntekterForSammenligningsgrunnlag.ainntektfilter
                ) {
                    respond(sammenligningsgrunnlagResponse())
                } else {
                    respondError(HttpStatusCode.InternalServerError)
                }
            }
        }
    }, mockStsRestClient)

    private fun ByteArray.getFilter() = objectMapper.readTree(this).get("ainntektsfilter").asText()

    init {
        Inntekter(testRapid, inntektRestClient)
    }

    @BeforeEach
    fun beforeEach() {
        testRapid.reset()
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

    @Test
    fun `mapper inn liste over arbeidsforhold på innteker`() {
        val start = YearMonth.of(2020, 3)
        val slutt = YearMonth.of(2021, 1)
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForSykepengegrunnlag))
        val inntekt0 =
            testRapid.inspektør.message(0).path("@løsning").path(Inntekter.Type.InntekterForSykepengegrunnlag.name)[0]
        val inntekt1 =
            testRapid.inspektør.message(0).path("@løsning").path(Inntekter.Type.InntekterForSykepengegrunnlag.name)[1]
        val inntekt2 =
            testRapid.inspektør.message(0).path("@løsning").path(Inntekter.Type.InntekterForSykepengegrunnlag.name)[2]

        assertTrue(inntekt0.path("arbeidsforholdliste").isEmpty)
        assertTrue(inntekt0.path("inntektsliste").isEmpty)

        val arbeidsforhold = inntekt1.path("arbeidsforholdliste")[0]
        assertEquals("frilanserOppdragstakerHonorarPersonerMm", arbeidsforhold.path("type").asText())
        assertEquals("orgnummer2", arbeidsforhold.path("orgnummer").asText())

        assertTrue(inntekt2.path("arbeidsInntektInformasjon").path("arbeidsforholdliste").isEmpty)
    }

    @Test
    fun `Mapper ikke ut arbeidsforholdliste for sammenligningsgrunnlag`() {
        val start = YearMonth.of(2020, 3)
        val slutt = YearMonth.of(2021, 1)
        testRapid.sendTestMessage(behov(start, slutt, Inntekter.Type.InntekterForSammenligningsgrunnlag))
        val inntekt =
            testRapid.inspektør.message(0).path("@løsning").path(Inntekter.Type.InntekterForSammenligningsgrunnlag.name)[2]

        val arbeidsforhold = inntekt.path("arbeidsforholdliste")
        assertTrue(arbeidsforhold.isEmpty)
    }

    private fun assertLøsning(behovType: Inntekter.Type, vararg yearsMonths: YearMonth) {
        assertTrue(testRapid.inspektør.message(0).hasNonNull("@løsning"))
        assertEquals(yearsMonths.toList(),
            testRapid.inspektør.message(0).path("@løsning").path(behovType.name)
                .map { it.path("årMåned").asYearMonth() }
        )
    }

    private fun behov(start: YearMonth, slutt: YearMonth, type: Inntekter.Type, id: UUID = UUID.randomUUID()) =
        objectMapper.writeValueAsString(behovMap(start, slutt, id, type))

    private fun behovMap(start: YearMonth, slutt: YearMonth, id: UUID, type: Inntekter.Type) = mapOf(
        "@id" to id,
        "@behov" to listOf(type.name),
        "fødselsnummer" to "123",
        "vedtaksperiodeId" to "vedtaksperiodeId",
        type.name to mapOf(
            "beregningStart" to "$start",
            "beregningSlutt" to "$slutt",
        )
    )

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
            "identifikator": "aktørId",
            "aktoerType": "AKTOER_ID"
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

}
