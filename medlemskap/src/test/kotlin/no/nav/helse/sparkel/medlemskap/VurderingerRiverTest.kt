package no.nav.helse.sparkel.medlemskap

import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import java.time.LocalDateTime
import java.util.UUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class VurderingerRiverTest {

    private val rapid: TestRapid = TestRapid().apply(::VurderingerRiver)

    private fun sendEvent(behov: String) = rapid.sendTestMessage(behov)

    private fun opprettLogglytter() = ListAppender<ILoggingEvent>().apply {
        (LoggerFactory.getLogger(VurderingerRiver::class.java) as Logger).addAppender(this)
        start()
    }

    @Test
    fun `behandler relevante meldinger`() {
        // Vi sjekker logg fordi vi ikke har database ennå
        val logglytter = opprettLogglytter()
        sendEvent(Meldinger.Ok)
        assertEquals(1, logglytter.list.filter { it.level == INFO }.size)
    }

    @Test
    fun `behandler ikke meldinger med uventede verdier`() {
        val logglytter = opprettLogglytter()
        sendEvent(Meldinger.Bogus)
        assertEquals(0, logglytter.list.filter { it.level == INFO }.size)
    }

    @Test
    fun `behandler ikke ikke-relevante meldinger`() {
        val logglytter = opprettLogglytter()
        sendEvent(Meldinger.NoeHeltAnnet)
        assertEquals(0, logglytter.list.filter { it.level == INFO }.size)
    }

    object Meldinger {
        @Language("json")
        internal val Ok = """
            {
                "soknadId": "${UUID.randomUUID()}",
                "speilSvar": "JA",
                "fnr": "12345678910"
            }
        """.trimIndent()

        @Language("json")
        internal val Bogus = """
            {
                "soknadId": "${UUID.randomUUID()}",
                "speilSvar": "JASSSS",
                "fnr": "12345678910"
            }
        """.trimIndent()

        @Language("json")
        internal val NoeHeltAnnet = """
            {
                "@event_name": "stans_automatisk_behandling",
                "@id": "${UUID.randomUUID()}",
                "@opprettet": "${LocalDateTime.now()}",
                "fødselsnummer": "11111100000",
                "status": "STOPP_AUTOMATIKK",
                "årsaker": [
                    "MEDISINSK_VILKAR"
                ],
                "opprettet": "${LocalDateTime.now()}",
                "originalMelding": "{}"
            }
        """.trimIndent()
    }
}
