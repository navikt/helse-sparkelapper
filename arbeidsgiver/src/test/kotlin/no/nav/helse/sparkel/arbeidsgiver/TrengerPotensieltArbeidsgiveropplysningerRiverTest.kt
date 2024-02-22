package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerPotensieltArbeidsgiveropplysningerRiver
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Test

internal class TrengerPotensieltArbeidsgiveropplysningerRiverTest {
    private val FNR = "fnr"
    private val ORGNUMMER = "orgnummer"

    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModules(JavaTimeModule())

    private val testRapid = TestRapid()
    private val mockproducer: KafkaProducer<String, TrengerArbeidsgiveropplysningerDto> = mockk(relaxed = true)

    init {
        TrengerPotensieltArbeidsgiveropplysningerRiver(testRapid, mockproducer)
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(eventTrengerPotensieltArbeidsgiveropplysninger("Tullebehov"))
        verify(exactly = 0) {
            mockproducer.send(any())
        }
    }

    @Test
    fun `publiserer forespørsel om potensielle arbeidsgiveropplysninger`() {
        every { mockproducer.send(any()) } answers { callOriginal() }
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(eventTrengerPotensieltArbeidsgiveropplysninger("trenger_potensielt_opplysninger_fra_arbeidsgiver", vedtaksperiodeId))

        verify(exactly = 1) {
            val trengerPotensieltArbeidsgiveropplysningerDto = mockTrengerPotensieltArbeidsgiveropplysninger(vedtaksperiodeId)
            val record = ProducerRecord(
                "tbd.arbeidsgiveropplysninger",
                null,
                FNR,
                trengerPotensieltArbeidsgiveropplysningerDto,
                listOf(RecordHeader("type", trengerPotensieltArbeidsgiveropplysningerDto.meldingstype))
            )
            mockproducer.send(record)
        }
    }

    private fun eventTrengerPotensieltArbeidsgiveropplysninger(eventName: String, vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to eventName,
                "@opprettet" to LocalDateTime.MAX,
                "fødselsnummer" to FNR,
                "organisasjonsnummer" to ORGNUMMER,
                "vedtaksperiodeId" to vedtaksperiodeId,
                "skjæringstidspunkt" to LocalDate.MIN,
                "sykmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN.plusDays(1), "tom" to LocalDate.MIN.plusDays(30))),
                "egenmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN)),
                "førsteFraværsdager" to listOf(mapOf("organisasjonsnummer" to ORGNUMMER, "førsteFraværsdag" to LocalDate.MIN))
            )
        ).toString()
}