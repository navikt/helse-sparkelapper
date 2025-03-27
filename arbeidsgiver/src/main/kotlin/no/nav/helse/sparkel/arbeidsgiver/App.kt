package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.kafka.AivenConfig
import com.github.navikt.tbd_libs.kafka.ConsumerProducerFactory
import com.github.navikt.tbd_libs.spedisjon.SpedisjonClient
import java.net.http.HttpClient
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerBegrensetDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerBegrensetRiver
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerRiver
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerIkkeArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerIkkeArbeidsgiveropplysningerRiver
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndertRiver
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndtertDto
import no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet.VedtaksperiodeForkastetDto
import no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet.VedtaksperiodeForkastetRiver
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("sparkel-arbeidsgiver")

fun main() {
    val env = System.getenv()

    val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModules(JavaTimeModule())

    val consumerProducerFactory = ConsumerProducerFactory(AivenConfig.default)
    val producer = ArbeidsgiveropplysningerProducer(consumerProducerFactory.createProducer(), objectMapper)

    val azureClient = createAzureTokenClientFromEnvironment(env)
    val spedisjonClient = SpedisjonClient(
        httpClient = HttpClient.newHttpClient(),
        objectMapper = objectMapper,
        tokenProvider = azureClient
    )

    val app = RapidApplication.create(env, consumerProducerFactory).apply {
        TrengerArbeidsgiveropplysningerRiver(this, producer)
        TrengerIkkeArbeidsgiveropplysningerRiver(this, producer)
        TrengerArbeidsgiveropplysningerBegrensetRiver(this, producer)
        InntektsmeldingHåndertRiver(this, producer, spedisjonClient)
        VedtaksperiodeForkastetRiver(this, producer)
    }
    logger.info("Hei, bro!")
    app.start()
}

internal class ArbeidsgiveropplysningerProducer(
    val producer: KafkaProducer<String, String>,
    val objectMapper: ObjectMapper,
) {

    fun send(payload: TrengerIkkeArbeidsgiveropplysningerDto) {
        payload.sendRecord(payload.fødselsnummer, payload.type)
    }

    fun send(payload: TrengerArbeidsgiveropplysningerDto) {
        payload.sendRecord(payload.fødselsnummer, payload.type)
    }

    fun send(payload: TrengerArbeidsgiveropplysningerBegrensetDto) {
        payload.sendRecord(payload.fødselsnummer, payload.type)
    }

    fun send(payload: InntektsmeldingHåndtertDto) {
        payload.sendRecord(payload.fødselsnummer, payload.type)
    }

    fun send(payload: VedtaksperiodeForkastetDto) {
        payload.sendRecord(payload.fødselsnummer, payload.type)
    }

    private fun Any.sendRecord(fnr: String, meldingtype: Meldingstype) {
        producer.send(record(fnr, meldingtype)).get()
    }

    private fun Any.record(fnr: String, meldingstype: Meldingstype): ProducerRecord<String, String> {
        return ProducerRecord(
            "tbd.arbeidsgiveropplysninger",
            null,
            fnr,
            objectMapper.writeValueAsString(this),
            listOf(RecordHeader("type", meldingstype.name.lowercase().toByteArray()))
        )
    }
}

