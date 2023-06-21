package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndertRiver
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndtertDto
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Test

class InntektsmeldingHåndertRiverTest {

    private val testRapid = TestRapid()
    private val mockproducer: KafkaProducer<String, InntektsmeldingHåndtertDto> = mockk(relaxed = true)

    init {
        InntektsmeldingHåndertRiver(testRapid, mockproducer)
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(
            inntektsmeldingHåndtert(
                eventName = "tull",
                vedtaksperiodeId = UUID.randomUUID()
            )
        )
        verify(exactly = 0) {
            mockproducer.send(any())
        }
    }

    @Test
    fun `leser gyldig event og sender det videre `() {
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(inntektsmeldingHåndtert(vedtaksperiodeId = vedtaksperiodeId))

        val payload = mockInntektsmeldingHåndtert(vedtaksperiodeId)
        verify(exactly = 1) {
            val record = ProducerRecord(
                "tbd.arbeidsgiveropplysninger",
                null,
                FNR,
                payload,
                listOf(RecordHeader("type", payload.meldingstype))
            )
            mockproducer.send(record)
        }
    }

    private fun inntektsmeldingHåndtert(
        eventName: String = "inntektsmelding_håndtert",
        vedtaksperiodeId: UUID
    ) = objectMapper.valueToTree<JsonNode>(
        mapOf(
            "@event_name" to eventName,
            "fødselsnummer" to FNR,
            "organisasjonsnummer" to ORGNUMMER,
            "vedtaksperiodeId" to vedtaksperiodeId,
            "inntektsmeldingId" to UUID.randomUUID()
        )
    ).toString()
}