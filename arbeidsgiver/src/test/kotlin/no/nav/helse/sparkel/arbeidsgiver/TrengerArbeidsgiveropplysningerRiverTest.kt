package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Refusjonsforslag
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerRiver
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class TrengerArbeidsgiveropplysningerRiverTest {
    private val FNR = "fnr"
    private val ORGNUMMER = "orgnummer"

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
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(eventMeldingMedInntekt("Tullebehov"))
        verify(exactly = 0) {
            mockproducer.send(any())
        }
    }

    @Test
    fun `ignorerer forespørsler fra arbeidsledig`() {
        testRapid.sendTestMessage(eventMeldingMedInntekt("trenger_opplysninger_fra_arbeidsgiver", organisasjonsnummer = "ARBEIDSLEDIG"))
        verify(exactly = 0) {
            mockproducer.send(any())
        }
    }

    @Test
    fun `publiserer forespørsel om arbeidsgiveropplysninger - med inntekt, refusjon, og agp`() {
        every { mockproducer.send(any()) } answers { callOriginal() }
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(eventMeldingMedInntekt("trenger_opplysninger_fra_arbeidsgiver", vedtaksperiodeId))

        verify(exactly = 1) {
            val trengerArbeidsgiveropplysningerDto = mockTrengerArbeidsgiveropplysningerMedInntekt(vedtaksperiodeId)
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
    fun `Videresender forrige inntekt i forespørsel`() {
        every { mockproducer.send(any()) } answers { callOriginal() }
        val vedtaksperiodeId = UUID.randomUUID()
        testRapid.sendTestMessage(eventMeldingMedForrigeInntekt(vedtaksperiodeId))

        verify(exactly = 1) {
            val trengerArbeidsgiveropplysningerDto = mockTrengerArbeidsgiveropplysningerMedForrigeInntekt(vedtaksperiodeId)
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

        verify(exactly = 1) {
            val trengerArbeidsgiveropplysningerDto = mockTrengerArbeidsgiverOpplysningerMedFastsattInntekt(vedtaksperiodeId)
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
    fun `vi logger error ved fastsatt inntekt uten fastsatt inntekt`() {
        testRapid.sendTestMessage(ugyldigFastsattInntektEvent())
        verify(exactly = 0) {
            mockproducer.send(any())
        }
        assertTrue(sikkerlogCollector.list.any { it.message.contains("forstod ikke trenger_opplysninger_fra_arbeidsgiver") })
    }

    private fun eventMeldingMedFastsattInntekt(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to "trenger_opplysninger_fra_arbeidsgiver",
                "@opprettet" to LocalDateTime.MAX,
                "fødselsnummer" to FNR,
                "organisasjonsnummer" to ORGNUMMER,
                "vedtaksperiodeId" to vedtaksperiodeId,
                "skjæringstidspunkt" to LocalDate.MIN,
                "førsteFraværsdager" to listOf(mapOf("organisasjonsnummer" to ORGNUMMER, "førsteFraværsdag" to LocalDate.MIN)),
                "sykmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN.plusDays(1), "tom" to LocalDate.MIN.plusDays(30))),
                "egenmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN)),
                "forespurteOpplysninger" to listOf(
                    mapOf(
                        "opplysningstype" to "FastsattInntekt",
                        "fastsattInntekt" to 10000.0
                    ),
                    mapOf(
                        "opplysningstype" to "Refusjon",
                        "forslag" to listOf(
                            mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN.plusDays(10), "beløp" to 10000.0),
                            mapOf("fom" to LocalDate.MIN.plusDays(11), "tom" to null, "beløp" to 9000.0)
                        )
                    ),
                    mapOf(
                        "opplysningstype" to "Arbeidsgiverperiode"
                    )
                ),
                "innhentInntektFraAOrdningen" to false
            )
        ).toString()

    private fun eventMeldingMedForrigeInntekt(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to "trenger_opplysninger_fra_arbeidsgiver",
                "@opprettet" to LocalDateTime.MAX,
                "fødselsnummer" to FNR,
                "organisasjonsnummer" to ORGNUMMER,
                "vedtaksperiodeId" to vedtaksperiodeId,
                "skjæringstidspunkt" to LocalDate.MIN,
                "førsteFraværsdager" to listOf(mapOf("organisasjonsnummer" to ORGNUMMER, "førsteFraværsdag" to LocalDate.MIN)),
                "sykmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN.plusDays(1), "tom" to LocalDate.MIN.plusDays(30))),
                "egenmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN)),
                "forespurteOpplysninger" to listOf(
                    mapOf(
                        "opplysningstype" to "Inntekt",
                        "forslag" to mapOf("forrigeInntekt" to mapOf(
                            "skjæringstidspunkt" to LocalDate.MIN,
                                "kilde" to "INNTEKTSMELDING",
                                "beløp" to 31000.0
                            )
                        )
                    ),
                    mapOf(
                        "opplysningstype" to "Refusjon",
                        "forslag" to emptyList<Refusjonsforslag>()
                    ),
                    mapOf(
                        "opplysningstype" to "Arbeidsgiverperiode"
                    )
                ),
                "innhentInntektFraAOrdningen" to false
            )
        ).toString()

    private fun eventMeldingMedInntekt(eventName: String, vedtaksperiodeId: UUID = UUID.randomUUID(), organisasjonsnummer: String = ORGNUMMER): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to eventName,
                "@opprettet" to LocalDateTime.MAX,
                "fødselsnummer" to FNR,
                "organisasjonsnummer" to organisasjonsnummer,
                "vedtaksperiodeId" to vedtaksperiodeId,
                "skjæringstidspunkt" to LocalDate.MIN,
                "førsteFraværsdager" to listOf(mapOf("organisasjonsnummer" to ORGNUMMER, "førsteFraværsdag" to LocalDate.MIN)),
                "sykmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN.plusDays(1), "tom" to LocalDate.MIN.plusDays(30))),
                "egenmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN)),
                "forespurteOpplysninger" to listOf(
                    mapOf(
                        "opplysningstype" to "Inntekt",
                        "forslag" to mapOf("forrigeInntekt" to null)
                    ),
                    mapOf(
                        "opplysningstype" to "Refusjon",
                        "forslag" to emptyList<Refusjonsforslag>()
                    ),
                    mapOf(
                        "opplysningstype" to "Arbeidsgiverperiode"
                    )
                ),
                "innhentInntektFraAOrdningen" to false
            )
        ).toString()

    private fun ugyldigInntektEvent(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        mapUtenForespurteOpplysninger(vedtaksperiodeId)
            .plus("forespurteOpplysninger" to listOf(mapOf("opplysningstype" to "Inntekt", "forslag" to null)))
            .toJson()

    private fun ugyldigFastsattInntektEvent(vedtaksperiodeId: UUID = UUID.randomUUID()): String =
        mapUtenForespurteOpplysninger(vedtaksperiodeId)
            .plus("forespurteOpplysninger" to listOf(mapOf("opplysningstype" to "FastsattInntekt")))
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
        "førsteFraværsdager" to listOf(mapOf("organisasjonsnummer" to ORGNUMMER, "førsteFraværsdag" to LocalDate.MIN)),
        "egenmeldingsperioder" to emptyList<Map<String, LocalDate>>(),
        "skjæringstidspunkt" to LocalDate.MIN,
        "sykmeldingsperioder" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MAX))
    )
}
