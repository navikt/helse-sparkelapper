package no.nav.helse.sparkel.tilbakedatert

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import java.util.UUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TilbakedatertRiverTest {

    private val rapid: TestRapid = TestRapid().apply(::TilbakedatertRiver)

    private fun sendEvent(behov: String) = rapid.sendTestMessage(behov)

    @Test
    fun `Sender ikke tilbakedatering_behandlet for OK sykmelding`() {
        sendEvent(Meldinger.Ok)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet for sykmelding som er under behandling`() {
        sendEvent(Meldinger.UnderBehandling)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Sender tilbakedatering_behandlet for godkjente sykmeldinger`() {
        sendEvent(Meldinger.Godkjent)
        assertEquals(1, rapid.inspektør.size)
        val expectedSykmeldingId = jacksonObjectMapper().readTree(Meldinger.Godkjent)["sykmelding"]["id"].asText()
        assertEquals(expectedSykmeldingId, rapid.inspektør.field(0, "sykmeldingId").asText())
        val expectedPerioder = jacksonObjectMapper().readTree(Meldinger.Godkjent)["sykmelding"]["aktivitet"]
        assertEquals(expectedPerioder, rapid.inspektør.field(0, "perioder"))
    }

    @Test
    fun `Sender tilbakedatering_behandlet for godkjente sykmeldinger hvor det har vært etterspurt flere opplysninger`() {
        sendEvent(Meldinger.GodkjentEtterFlereOpplysninger)
        assertEquals(1, rapid.inspektør.size)
        val expectedSykmeldingId = jacksonObjectMapper().readTree(Meldinger.GodkjentEtterFlereOpplysninger)["sykmelding"]["id"].asText()
        assertEquals(expectedSykmeldingId, rapid.inspektør.field(0, "sykmeldingId").asText())
        val expectedPerioder = jacksonObjectMapper().readTree(Meldinger.GodkjentEtterFlereOpplysninger)["sykmelding"]["aktivitet"]
        assertEquals(expectedPerioder, rapid.inspektør.field(0, "perioder"))
    }

    object Meldinger {
        @Language("JSON")
        internal val Ok = """
            {
                "sykmelding": {
                    "id": "${UUID.randomUUID()}",
                    "pasient": {
                        "fnr": "424242"
                    },
                    "aktivitet": []
                },
                "validation": {
                    "status": "OK",
                    "timestamp": "2025-03-26T23:00:00Z",
                    "rules": []
                }
            }
        """.trimIndent()

        @Language("JSON")
        internal val UnderBehandling = """
            {
                "sykmelding": {
                    "id": "${UUID.randomUUID()}",
                    "pasient": {
                        "fnr": "424242"
                    },
                    "aktivitet": []
                },
                "validation": {
                    "status": "PENDING",
                    "timestamp": "2025-06-03T15:15:15.167486189Z",
                    "rules": [
                        {
                            "name": "TILBAKEDATERING_UNDER_BEHANDLING",
                            "timestamp": "2025-06-03T15:15:15.167486189Z",
                            "validationType": "AUTOMATIC",
                            "reason": {
                                "sykmeldt": "Sykmeldingen blir manuelt behandlet fordi den er tilbakedatert",
                                "sykmelder": "Sykmeldingen er til manuell behandling"
                            },
                            "type": "PENDING"
                        }
                    ]
                }
            }
        """.trimIndent()

        @Language("JSON")
        internal val Godkjent = """
            {
                "sykmelding": {
                    "id": "${UUID.randomUUID()}",
                    "pasient": {
                        "fnr": "424242"
                    },
                    "aktivitet": [
                        {
                            "fom": "2025-03-27",
                            "tom": "2025-04-02"
                        }
                    ]
                },
                "validation": {
                    "status": "OK",
                    "timestamp": "2025-06-03T15:16:30.163788143Z",
                    "rules": [
                        {
                            "name": "TILBAKEDATERING_UNDER_BEHANDLING",
                            "timestamp": "2025-06-03T15:16:30.163788143Z",
                            "validationType": "MANUAL",
                            "type": "OK"
                        },
                        {
                            "name": "TILBAKEDATERING_UNDER_BEHANDLING",
                            "timestamp": "2025-06-03T15:15:15.167486189Z",
                            "validationType": "AUTOMATIC",
                            "reason": {
                                "sykmeldt": "Sykmeldingen blir manuelt behandlet fordi den er tilbakedatert",
                                "sykmelder": "Sykmeldingen er til manuell behandling"
                            },
                            "type": "PENDING"
                        }
                    ]
                }
            }
        """.trimIndent()

        @Language("JSON")
        internal val GodkjentEtterFlereOpplysninger = """
            {
                "sykmelding": {
                    "id": "${UUID.randomUUID()}",
                    "pasient": {
                        "fnr": "424242"
                    },
                    "aktivitet": [
                        {
                            "fom": "2025-03-27",
                            "tom": "2025-04-02"
                        }
                    ]
                },
                "validation": {
                    "status": "OK",
                    "timestamp": "2025-08-13T07:38:48.595569968Z",
                    "rules": [
                        {
                            "name": "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER",
                            "timestamp": "2025-08-13T07:38:48.595569968Z",
                            "validationType": "MANUAL",
                            "type": "OK"
                        },
                        {
                            "name": "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER",
                            "timestamp": "2025-08-05T08:45:40.789871804Z",
                            "validationType": "MANUAL",
                            "reason": {
                                "sykmeldt": "Sykmeldingen blir manuelt behandlet fordi den er tilbakedatert",
                                "sykmelder": "Tilbakedatering krever flere opplysninger"
                            },
                            "type": "PENDING"
                        },
                        {
                            "name": "TILBAKEDATERING_UNDER_BEHANDLING",
                            "timestamp": "2025-07-17T12:51:59.379051234Z",
                            "validationType": "AUTOMATIC",
                            "reason": {
                                "sykmeldt": "Sykmeldingen blir manuelt behandlet fordi den er tilbakedatert",
                                "sykmelder": "Sykmeldingen er til manuell behandling"
                            },
                            "type": "PENDING"
                        }
                    ]
                }
            }
        """.trimIndent()
    }
}
