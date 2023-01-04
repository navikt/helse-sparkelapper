package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class TrengerArbeidsgiveropplysningerRiverTest {
    private val FNR = "1111111111"
    private val ORGNUMMER = "222222222"

    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModules(JavaTimeModule())

    private val testRapid = TestRapid()
    private val mockproducer: KafkaProducer<String, TrengerArbeidsgiveropplysningerDto> = mockk(relaxed = true)

    private val logCollector = ListAppender<ILoggingEvent>()
    private val sikkerlogCollector = ListAppender<ILoggingEvent>()

    init {
        TrengerArbeidsgiveropplysningerRiver(testRapid, mockproducer)
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
    fun `logger ved gyldig event`() {
        testRapid.sendTestMessage(eventMeldingMedInntekt("trenger_opplysninger_fra_arbeidsgiver"))
        assertEquals(2, logCollector.list.size)
        assertTrue(logCollector.list.any { it.message.contains("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis") })
        assertTrue(logCollector.list.any { it.message.contains("Publiserte forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger") })
        assertEquals(2, sikkerlogCollector.list.size)
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis med data") })
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Publiserte forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger") })
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(eventMeldingMedInntekt("Tullebehov"))
        assertEquals(0, logCollector.list.size)
        assertEquals(0, sikkerlogCollector.list.size)
    }

    @Test
    fun `publiserer forespørsel om arbeidsgiveropplysninger - med inntekt, refusjon, og agp`() {
        every { mockproducer.send(any()) } answers { callOriginal() }
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(eventMeldingMedInntekt("trenger_opplysninger_fra_arbeidsgiver", vedtaksperiodeId))

        assertEquals(1, testRapid.inspektør.size)
        verify {
            val trengerArbeidsgiveropplysningerDto = TrengerArbeidsgiveropplysningerDto(
                Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER,
                FNR,
                ORGNUMMER,
                vedtaksperiodeId,
                listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MAX)),
                forespurtData = listOf(
                    mapOf(
                        "opplysningstype" to "Inntekt",
                        "forslag" to mapOf("beregningsmåneder" to listOf(
                            YearMonth.of(2022, 8),
                            YearMonth.of(2022, 9),
                            YearMonth.of(2022, 10)
                        ))
                    ),
                    mapOf("opplysningstype" to "Refusjon"),
                    mapOf(
                        "opplysningstype" to "Arbeidsgiverperiode",
                        "forslag" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN.plusDays(15)))
                    )
                ),
                opprettet = LocalDateTime.MAX
            )
            val record = ProducerRecord(
                "tbd.arbeidsgiveropplysninger",
                null,
                FNR,
                trengerArbeidsgiveropplysningerDto,
                listOf(RecordHeader("type", trengerArbeidsgiveropplysningerDto.meldingstype))
            )
            mockproducer.send(record)
        }
    }

    @Test
    fun `publiserer forespørsel om arbeidsgiveropplysninger - med fastsatt inntekt, refusjon, og agp`() {
        every { mockproducer.send(any()) } answers { callOriginal() }
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(eventMeldingMedFastsattInntekt(vedtaksperiodeId))

        assertEquals(1, testRapid.inspektør.size)
        verify {
            val trengerArbeidsgiveropplysningerDto = TrengerArbeidsgiveropplysningerDto(
                Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER,
                FNR,
                ORGNUMMER,
                vedtaksperiodeId,
                listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MAX)),
                forespurtData = listOf(
                    mapOf(
                        "opplysningstype" to "FastsattInntekt",
                        "fastsattInntekt" to 10000.0
                    ),
                    mapOf("opplysningstype" to "Refusjon"),
                    mapOf(
                        "opplysningstype" to "Arbeidsgiverperiode",
                        "forslag" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN.plusDays(15)))
                    )
                ),
                opprettet = LocalDateTime.MAX
            )
            val record = ProducerRecord(
                "tbd.arbeidsgiveropplysninger",
                null,
                FNR,
                trengerArbeidsgiveropplysningerDto,
                listOf(RecordHeader("type", trengerArbeidsgiveropplysningerDto.meldingstype))
            )
            mockproducer.send(record)
        }
    }

    @Test
    fun `vi logger error ved inntekt uten beregningsmåneder`() {
        testRapid.sendTestMessage(ugyldigInntektEvent())
        assertTrue(sikkerlogCollector.list.any { it.message.contains("forstod ikke trenger_opplysninger_fra_arbeidsgiver") })
    }

    @Test
    fun `vi logger error ved fastsatt inntekt uten fastsatt inntekt`() {
        testRapid.sendTestMessage(ugyldigFastsattInntektEvent())
        assertTrue(sikkerlogCollector.list.any { it.message.contains("forstod ikke trenger_opplysninger_fra_arbeidsgiver") })
    }

    @Test
    fun `vi logger error ved arbeidsgiverperiode uten forslag`() {
        testRapid.sendTestMessage(ugyldigArbeidsperiodeEvent())
        assertTrue(sikkerlogCollector.list.any { it.message.contains("forstod ikke trenger_opplysninger_fra_arbeidsgiver") })
    }

    private fun eventMeldingMedInntekt(eventName: String, vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to eventName,
                "@opprettet" to LocalDateTime.MAX,
                "fødselsnummer" to FNR,
                "organisasjonsnummer" to ORGNUMMER,
                "vedtaksperiodeId" to vedtaksperiodeId,
                "sykmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MAX)),
                "forespurteOpplysninger" to listOf(
                    mapOf(
                        "opplysningstype" to "Inntekt",
                        "forslag" to mapOf("beregningsmåneder" to listOf(
                            YearMonth.of(2022, 8),
                            YearMonth.of(2022, 9),
                            YearMonth.of(2022, 10)
                        ))
                    ),
                    mapOf("opplysningstype" to "Refusjon"),
                    mapOf(
                        "opplysningstype" to "Arbeidsgiverperiode",
                        "forslag" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN.plusDays(15)))
                    )
                )
            )
        ).toString()

    private fun eventMeldingMedFastsattInntekt(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to "trenger_opplysninger_fra_arbeidsgiver",
                "@opprettet" to LocalDateTime.MAX,
                "fødselsnummer" to FNR,
                "organisasjonsnummer" to ORGNUMMER,
                "vedtaksperiodeId" to vedtaksperiodeId,
                "sykmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MAX)),
                "forespurteOpplysninger" to listOf(
                    mapOf(
                        "opplysningstype" to "FastsattInntekt",
                        "fastsattInntekt" to 10000.0
                    ),
                    mapOf("opplysningstype" to "Refusjon"),
                    mapOf(
                        "opplysningstype" to "Arbeidsgiverperiode",
                        "forslag" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN.plusDays(15)))
                    )
                )
            )
        ).toString()

    private fun ugyldigInntektEvent(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        mapUtenForespurteOpplysninger(vedtaksperiodeId)
            .plus("forespurteOpplysninger" to listOf(mapOf("opplysningstype" to "Inntekt")))
            .toJson()

    private fun ugyldigFastsattInntektEvent(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        mapUtenForespurteOpplysninger(vedtaksperiodeId)
            .plus("forespurteOpplysninger" to listOf(mapOf("opplysningstype" to "FastsattInntekt")))
            .toJson()
    private fun ugyldigArbeidsperiodeEvent(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        mapUtenForespurteOpplysninger(vedtaksperiodeId)
            .plus("forespurteOpplysninger" to listOf(mapOf("opplysningstype" to "Arbeidsgiverperiode")))
            .toJson()

    private fun Map<String, Any>.toJson() = objectMapper.valueToTree<JsonNode>(this).toString()

    private fun mapUtenForespurteOpplysninger(vedtaksperiodeId: UUID) = mapOf(
        "@id" to UUID.randomUUID(),
        "@event_name" to "trenger_opplysninger_fra_arbeidsgiver",
        "@opprettet" to LocalDateTime.MAX,
        "fødselsnummer" to FNR,
        "organisasjonsnummer" to ORGNUMMER,
        "vedtaksperiodeId" to vedtaksperiodeId,
        "fom" to LocalDate.MIN,
        "tom" to LocalDate.MAX,
    )
}
