package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerRiver
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerIkkeArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerIkkeArbeidsgiveropplysningerRiver
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class TrengerIkkeArbeidsgiveropplysningerRiverTest {
    private val testRapid = TestRapid()
    private val mockproducer: ArbeidsgiveropplysningerProducer = mockk(relaxed = true)

    private val logCollector = ListAppender<ILoggingEvent>()
    private val sikkerlogCollector = ListAppender<ILoggingEvent>()

    init {
        TrengerIkkeArbeidsgiveropplysningerRiver(testRapid, mockproducer)
        (LoggerFactory.getLogger(TrengerArbeidsgiveropplysningerRiver::class.java) as Logger).addAppender(logCollector)
        logCollector.start()
        (LoggerFactory.getLogger("tjenestekall") as Logger).addAppender(sikkerlogCollector)
        sikkerlogCollector.start()
    }

    @BeforeEach
    fun beforeEach() {
        logCollector.list.clear()
        sikkerlogCollector.list.clear()
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(event("Tullebehov"))
        verify(exactly = 0) {
            mockproducer.send(any<TrengerIkkeArbeidsgiveropplysningerDto>())
        }
    }

    @Test
    fun `publiserer at det ikke trengs forespørsel om arbeidsgiveropplysninger`() {
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(event(
            eventName = "trenger_ikke_opplysninger_fra_arbeidsgiver",
            vedtaksperiodeId = vedtaksperiodeId
        ))
        val trengerIkkeArbeidsgiveropplysningerDto = mockTrengerIkkeArbeidsgiveropplysningerDto(vedtaksperiodeId)
        verify(exactly = 1) {
            mockproducer.send(trengerIkkeArbeidsgiveropplysningerDto)
        }
    }

    @Test
    fun `logger error og sender ikke beskjed om at vi ikke trenger forespørsler ved ugyldig event`() {
        testRapid.sendTestMessage(eventUtenFødselsnummer())
        testRapid.sendTestMessage(eventUtenOrgnummer())
        testRapid.sendTestMessage(eventUtenVedtaksperiodeId())
        testRapid.sendTestMessage(eventUtenOpprettet())

        assertEquals(4, sikkerlogCollector.list.filter { it.message.contains("forstod ikke trenger_ikke_opplysninger_fra_arbeidsgiver") }.size)
        verify(exactly = 0) {
            mockproducer.send(any<TrengerIkkeArbeidsgiveropplysningerDto>())
        }
    }

    @Language("JSON")
    private fun event(
        eventName: String = "trenger_ikke_opplysninger_fra_arbeidsgiver",
        vedtaksperiodeId: UUID? = UUID.randomUUID()
    ): String = """{
        "@event_name": "$eventName",
        "fødselsnummer": "fnr", 
        "organisasjonsnummer": "orgnummer", 
         "vedtaksperiodeId": "$vedtaksperiodeId",
        "@opprettet": "${LocalDateTime.MAX}"
    }"""

    @Language("JSON")
    private fun eventUtenFødselsnummer(): String = """{
        "@event_name": "trenger_ikke_opplysninger_fra_arbeidsgiver",
        "organisasjonsnummer": "orgnummer", 
         "vedtaksperiodeId": "${UUID.randomUUID()}",
        "@opprettet": "${LocalDateTime.MAX}"
    }"""

    @Language("JSON")
    private fun eventUtenOrgnummer(): String = """{
        "@event_name": "trenger_ikke_opplysninger_fra_arbeidsgiver",
        "fødselsnummer": "fnr",
         "vedtaksperiodeId": "${UUID.randomUUID()}",
        "@opprettet": "${LocalDateTime.MAX}"
    }"""

    @Language("JSON")
    private fun eventUtenVedtaksperiodeId(): String = """{
        "@event_name": "trenger_ikke_opplysninger_fra_arbeidsgiver",
        "fødselsnummer": "fnr",
        "organisasjonsnummer": "orgnummer", 
        "@opprettet": "${LocalDateTime.MAX}"
    }"""

    @Language("JSON")
    private fun eventUtenOpprettet(): String = """{
        "@event_name": "trenger_ikke_opplysninger_fra_arbeidsgiver",
        "fødselsnummer": "fnr",
        "organisasjonsnummer": "orgnummer"
    }"""
}
