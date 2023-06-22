package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndertRiver
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndtertDto
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRepository
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Test

class InntektsmeldingHåndertRiverTest {

    private val testRapid = TestRapid()
    private val mockProducer: KafkaProducer<String, InntektsmeldingHåndtertDto> = mockk(relaxed = true)
    private val mockRepository: InntektsmeldingRegistrertRepository = mockk(relaxed = true)

    init {
        InntektsmeldingHåndertRiver(testRapid, mockProducer, mockRepository)
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(
            inntektsmeldingHåndtert(eventName = "tull")
        )
        verify(exactly = 0) {
            mockProducer.send(any())
        }
    }

    @Test
    fun `leser gyldig event og sender det videre `() {
        val dokumentId = UUID.randomUUID()
        val hendelseId = UUID.randomUUID()
        every { mockRepository.finnDokumentId(hendelseId) } returns dokumentId

        val vedtaksperiodeId = UUID.randomUUID()

        testRapid.sendTestMessage(inntektsmeldingHåndtert(vedtaksperiodeId = vedtaksperiodeId, hendelseId = hendelseId))

        val payload = mockInntektsmeldingHåndtert(vedtaksperiodeId, dokumentId)
        verify(exactly = 1) {
            val record = ProducerRecord(
                "tbd.arbeidsgiveropplysninger",
                null,
                FNR,
                payload,
                listOf(RecordHeader("type", payload.meldingstype))
            )
            mockProducer.send(record)
        }
    }

    @Test
    fun `leser gyldig event og sender det videre, selvom man ikke finner inntektsmeldingens dokumentId`() {
        every { mockRepository.finnDokumentId(any()) } returns null

        val vedtaksperiodeId = UUID.randomUUID()
        val hendelseId = UUID.randomUUID()
        val dokumentId = mockRepository.finnDokumentId(hendelseId)
        testRapid.sendTestMessage(inntektsmeldingHåndtert(vedtaksperiodeId = vedtaksperiodeId, hendelseId = hendelseId))

        val payload = mockInntektsmeldingHåndtert(vedtaksperiodeId, dokumentId)
        verify(exactly = 1) {
            val record = ProducerRecord(
                "tbd.arbeidsgiveropplysninger",
                null,
                FNR,
                payload,
                listOf(RecordHeader("type", payload.meldingstype))
            )
            mockProducer.send(record)
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
            "inntektsmeldingId" to hendelseId
        )
    ).toString()
}