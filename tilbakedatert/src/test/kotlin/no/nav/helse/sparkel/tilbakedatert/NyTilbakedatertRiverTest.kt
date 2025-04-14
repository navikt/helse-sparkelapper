package no.nav.helse.sparkel.tilbakedatert

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import java.util.UUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NyTilbakedatertRiverTest {

    private val rapid: TestRapid = TestRapid().apply(::NyTilbakedatertRiver)

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
    }

    object Meldinger {
        @Language("JSON")
        internal val Ok = """
            {
                "sykmelding": {
                    "id": "${UUID.randomUUID()}",
                    "pasient": {
                        "id": "424242"
                    }
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
                        "id": "424242"
                    }
                },
                "validation": {
                    "status": "PENDING",
                    "timestamp": "2025-04-04T14:15:15.597552892Z",
                    "rules": [
                        {
                            "name": "TILBAKEDATERING_UNDER_BEHANDLING",
                            "timestamp": "2025-04-04T14:15:15.597552892Z",
                            "description": "Sykmeldingen er til manuell behandling",
                            "validationType": "AUTOMATIC",
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
                    "timestamp": "2025-04-04T14:52:34.308156427Z",
                    "rules": [
                        {
                            "name": "TILBAKEDATERING_UNDER_BEHANDLING",
                            "description": "Sykmeldingen er til manuell behandling",
                            "timestamp": "2025-04-04T14:52:34.308156427Z",
                            "validationType": "MANUAL",
                            "type": "OK",
                            "outcome": {
                                "outcome": "OK",
                                "timestamp": "2025-04-04T14:52:34.308156427Z"
                            }
                        },
                        {
                            "name": "TILBAKEDATERING_UNDER_BEHANDLING",
                            "timestamp": "2025-04-04T14:15:15.597552892Z",
                            "description": "Sykmeldingen er til manuell behandling",
                            "validationType": "AUTOMATIC",
                            "type": "PENDING"
                        }
                    ]
                }
            }
        """.trimIndent()
    }
}
