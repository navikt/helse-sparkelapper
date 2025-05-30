package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.spedisjon.HentMeldingResponse
import com.github.navikt.tbd_libs.spedisjon.SpedisjonClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndertRiver
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndtertDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InntektsmeldingHåndertRiverTest {

    private val testRapid = TestRapid()
    private val mockProducer: ArbeidsgiveropplysningerProducer = mockk(relaxed = true)
    private val spedisjonClient = mockk<SpedisjonClient>()

    init {
        InntektsmeldingHåndertRiver(testRapid, mockProducer, spedisjonClient)
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(
            inntektsmeldingHåndtert(eventName = "tull")
        )
        verify(exactly = 0) {
            mockProducer.send(any<InntektsmeldingHåndtertDto>())
        }
    }

    @Test
    fun `leser gyldig event og sender det videre `() {
        val dokumentId = UUID.randomUUID()
        val hendelseId = UUID.randomUUID()
        every { spedisjonClient.hentMelding(hendelseId, any()) } returns HentMeldingResponse(
            type = "denne_type_kan_være_hva_som_helst_fordi_sparkel_arbeidsgiver_bare_tar_ekstern_dokument_id_uansett",
            fnr = "123456678911",
            internDokumentId = hendelseId,
            eksternDokumentId = dokumentId,
            rapportertDato = LocalDateTime.now(),
            duplikatkontroll = "",
            jsonBody = "{}"
        ).ok()

        val vedtaksperiodeId = UUID.randomUUID()

        testRapid.sendTestMessage(inntektsmeldingHåndtert(vedtaksperiodeId = vedtaksperiodeId, hendelseId = hendelseId))

        val payload = mockInntektsmeldingHåndtert(vedtaksperiodeId, dokumentId)
        verify(exactly = 1) {
            mockProducer.send(payload)
        }
    }

    @Test
    fun `kaster exception hvis man ikke finner inntektsmeldingens dokumentId`() {
        every { spedisjonClient.hentMelding(any(), any()) } returns Result.Error("Fikk en feil", null)

        val vedtaksperiodeId = UUID.randomUUID()
        val hendelseId = UUID.randomUUID()

        assertThrows<RuntimeException> {
            testRapid.sendTestMessage(inntektsmeldingHåndtert(vedtaksperiodeId = vedtaksperiodeId, hendelseId = hendelseId))
        }

        verify(exactly = 0) {
            mockProducer.send(any<InntektsmeldingHåndtertDto>())
        }
    }

    private fun inntektsmeldingHåndtert(
        eventName: String = "inntektsmelding_håndtert",
        vedtaksperiodeId: UUID = UUID.randomUUID(),
        hendelseId: UUID = UUID.randomUUID()
    ) = objectMapper.valueToTree<JsonNode>(
        mapOf(
            "@event_name" to eventName,
            "fødselsnummer" to FNR,
            "organisasjonsnummer" to ORGNUMMER,
            "vedtaksperiodeId" to vedtaksperiodeId,
            "inntektsmeldingId" to hendelseId,
            "@opprettet" to LocalDateTime.MAX
        )
    ).toString()
}
