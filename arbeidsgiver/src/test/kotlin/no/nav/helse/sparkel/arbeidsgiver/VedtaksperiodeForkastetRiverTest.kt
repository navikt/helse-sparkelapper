package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet.VedtaksperiodeForkastetDto
import no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet.VedtaksperiodeForkastetRiver
import org.junit.jupiter.api.Test

internal class VedtaksperiodeForkastetRiverTest {

    private val testRapid = TestRapid()
    private val mockproducer: ArbeidsgiveropplysningerProducer = mockk(relaxed = true)

    init {
        VedtaksperiodeForkastetRiver(testRapid, mockproducer)
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(forkastetVedtaksperiode(eventName = "tull"))
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }

    @Test
    fun `ignorerer arbeidsledige`() {
        testRapid.sendTestMessage(forkastetVedtaksperiode(orgnummer = "ARBEIDSLEDIG"))
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }
    @Test
    fun `ignorerer frilansere`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(orgnummer = "FRILANS")
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }

    @Test
    fun `ignorerer selvstendige`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(orgnummer = "SELVSTENDIG")
        )
        verify(exactly = 0) {
            mockproducer.send(any<VedtaksperiodeForkastetDto>())
        }
    }


    @Test
    fun `leser event og sender videre`() {
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(forkastetVedtaksperiode(vedtaksperiodeId = vedtaksperiodeId))
        val payload = VedtaksperiodeForkastetDto(
            fødselsnummer = FNR,
            organisasjonsnummer = ORGNUMMER,
            vedtaksperiodeId = vedtaksperiodeId,
            opprettet = LocalDateTime.MAX
        )

        verify {
            mockproducer.send(payload)
        }
    }

    private fun forkastetVedtaksperiode(
        vedtaksperiodeId: UUID = UUID.randomUUID(),
        eventName: String = "vedtaksperiode_forkastet",
        orgnummer: String = ORGNUMMER,
    ) = objectMapper.valueToTree<JsonNode>(
        mapOf(
            "@event_name" to eventName,
            "fødselsnummer" to FNR,
            "organisasjonsnummer" to orgnummer,
            "vedtaksperiodeId" to vedtaksperiodeId,
            "@opprettet" to LocalDateTime.MAX
        )
    ).toString()
}
