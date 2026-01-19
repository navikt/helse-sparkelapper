package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerBegrensetRiver
import no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet.VedtaksperiodeForkastetDto
import org.junit.jupiter.api.Test

internal class TrengerArbeidsgiveropplysningerBegrensetRiverTest {

    private val testRapid = TestRapid()
    private val mockproducer: ArbeidsgiveropplysningerProducer = mockk(relaxed = true)

    init {
        TrengerArbeidsgiveropplysningerBegrensetRiver(testRapid, mockproducer)
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                UUID.randomUUID(),
                LocalDate.MIN,
                LocalDate.MIN.plusDays(15),
                eventName = "tull",
                tilstand = "START"
            )
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }

    @Test
    fun `ignorerer arbeidsledige`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                UUID.randomUUID(),
                LocalDate.MIN,
                LocalDate.MIN.plusDays(15),
                eventName = "vedtaksperiode_forkastet",
                orgnummer = "ARBEIDSLEDIG",
                tilstand = "START"
            )
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }
    @Test
    fun `ignorerer frilansere`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                UUID.randomUUID(),
                LocalDate.MIN,
                LocalDate.MIN.plusDays(15),
                eventName = "vedtaksperiode_forkastet",
                orgnummer = "FRILANS",
                tilstand = "START"
            )
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }

    @Test
    fun `ignorerer selvstendige`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                UUID.randomUUID(),
                LocalDate.MIN,
                LocalDate.MIN.plusDays(15),
                eventName = "vedtaksperiode_forkastet",
                orgnummer = "SELVSTENDIG",
                tilstand = "START"
            )
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }

    @Test
    fun `leser event og sender videre når trengerArbeidsgiveropplysninger=true og som er forkastet i tilstand START`() {
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                vedtaksperiodeId = vedtaksperiodeId,
                fom = LocalDate.MIN.plusDays(1),
                tom = LocalDate.MIN.plusDays(30),
                tilstand = "START",
            )
        )

        val payload = mockTrengerArbeidsgiverOpplysningerForespørAlt(vedtaksperiodeId)
        verify(exactly = 1) {
            mockproducer.send(payload)
        }
    }

    @Test
    fun `leser event og sender videre når trengerArbeidsgiveropplysninger=true og som er forkastet i tilstand AVVENTER_INFOTRYGDHISTORIKK`() {
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                vedtaksperiodeId = vedtaksperiodeId,
                fom = LocalDate.MIN.plusDays(1),
                tom = LocalDate.MIN.plusDays(30),
                tilstand = "AVVENTER_INFOTRYGDHISTORIKK",
            )
        )

        val payload = mockTrengerArbeidsgiverOpplysningerForespørAlt(vedtaksperiodeId)
        verify {
            mockproducer.send(payload)
        }
    }

    @Test
    fun `leser ikke inn event som er forkastet i annen tilstand enn AVVENTER_INFOTRYGDHISTORIKK eller START`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                UUID.randomUUID(),
                LocalDate.MIN,
                LocalDate.MIN.plusDays(16),
                tilstand = "tulletilstand"
            )
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }

    @Test
    fun `leser ikke inn event med trengerArbeidsgiveropplysninger=false`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                UUID.randomUUID(),
                fom = LocalDate.MIN,
                tom = LocalDate.MIN.plusDays(16),
                tilstand = "START",
                trengerArbeidsgiveropplysninger = false
            )
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }

    private fun forkastetVedtaksperiode(
        vedtaksperiodeId: UUID,
        fom: LocalDate,
        tom: LocalDate,
        eventName: String = "vedtaksperiode_forkastet",
        tilstand: String,
        orgnummer: String = ORGNUMMER,
        trengerArbeidsgiveropplysninger: Boolean = true,
    ) = objectMapper.valueToTree<JsonNode>(
        mapOf(
            "@event_name" to eventName,
            "fødselsnummer" to FNR,
            "yrkesaktivitetstype" to "ARBEIDSTAKER",
            "organisasjonsnummer" to orgnummer,
            "vedtaksperiodeId" to vedtaksperiodeId,
            "tilstand" to tilstand,
            "trengerArbeidsgiveropplysninger" to trengerArbeidsgiveropplysninger,
            "fom" to fom,
            "tom" to tom,
            "sykmeldingsperioder" to listOf(mapOf("fom" to fom, "tom" to tom)),
            "@opprettet" to LocalDateTime.MAX
        )
    ).toString()
}
