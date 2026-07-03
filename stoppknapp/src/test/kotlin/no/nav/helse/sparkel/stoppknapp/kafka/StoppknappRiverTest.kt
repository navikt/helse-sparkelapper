package no.nav.helse.sparkel.stoppknapp.kafka

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.helse.sparkel.stoppknapp.StoppknappRiver
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.util.UUID.randomUUID

internal class StoppknappRiverTest {
    private val testRapid = TestRapid()

    init {
        StoppknappRiver(testRapid)
    }

    @Test
    fun `Videresender stoppknappmeldinger fra isyfo`() {
        testRapid.sendTestMessage(stoppknappMelding())
        val svar = testRapid.inspektør.message(0)

        assertEquals("stans_automatisk_behandling", svar["@event_name"].asString())
        assertEquals("STOPP_AUTOMATIKK", svar["status"].asString())
        assertEquals(listOf("MEDISINSK_VILKAR"), svar["årsaker"].toList().map { it.asString() })
        assertEquals("12345678910", svar["fødselsnummer"].asString())
    }

    @Test
    fun `Videresender melding om oppheving av stans fra isyfo`() {
        testRapid.sendTestMessage(opphevingAvStansMelding())
        val svar = testRapid.inspektør.message(0)

        assertEquals("stans_automatisk_behandling", svar["@event_name"].asString())
        assertEquals("NORMAL", svar["status"].asString())
        assertEquals(emptyList<String>(), svar["årsaker"].toList().map { it.asString() })
        assertEquals("12345678910", svar["fødselsnummer"].asString())
    }

    @Language("JSON")
    private fun stoppknappMelding(opprettet: LocalDateTime = now()): String =
        """
        {
            "uuid": "${randomUUID()}",
            "veilederIdent": {
                "value": "TULLE_IDENT"
            },
            "sykmeldtFnr": {
                "value": "12345678910"
            },
            "status": "STOPP_AUTOMATIKK",
            "arsakList": [
                {
                    "type": "MEDISINSK_VILKAR"
                }
            ],
            "virksomhetNr": {
                "value": "TULLE_VIRKSOMHET"
            },
            "opprettet": "${opprettet.toInstant()}",
            "enhetNr": {
                "value": "TULLE_ENHET"
            }
        }
        """.trimIndent()

    @Language("JSON")
    private fun opphevingAvStansMelding(): String =
        """
        {
            "uuid": "${randomUUID()}",
            "veilederIdent": {
                "value": "TULLE_IDENT"
            },
            "sykmeldtFnr": {
                "value": "12345678910"
            },
            "status": "NORMAL",
            "virksomhetNr": {
                "value": "TULLE_VIRKSOMHET"
            },
            "opprettet": "${now().toInstant()}",
            "enhetNr": {
                "value": "TULLE_ENHET"
            }
        }
        """.trimIndent()

    private fun LocalDateTime.toInstant() = toInstant(ZoneId.systemDefault().rules.getOffset(this))
}
