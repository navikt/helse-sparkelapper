package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerBegrensetRiver
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Test

internal class TrengerArbeidsgiveropplysningerBegrensetRiverTest {

    private val testRapid = TestRapid()
    private val mockproducer: KafkaProducer<String, TrengerArbeidsgiveropplysningerDto> = mockk(relaxed = true)

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
            mockproducer.send(any())
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
            mockproducer.send(any())
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
            mockproducer.send(any())
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
            mockproducer.send(any())
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

        val payload = mockTrengerArbeidsgiverOpplysningerUtenForslag(vedtaksperiodeId)
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

        val payload = mockTrengerArbeidsgiverOpplysningerUtenForslag(vedtaksperiodeId)
        verify {
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
            mockproducer.send(any())
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
            mockproducer.send(any())
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
