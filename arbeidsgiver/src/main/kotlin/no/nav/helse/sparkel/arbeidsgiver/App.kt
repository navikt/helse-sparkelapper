package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import java.util.Properties
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerBegrensetRiver
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerRiver
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerIkkeArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerIkkeArbeidsgiveropplysningerRiver
import no.nav.helse.sparkel.arbeidsgiver.db.Database
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndertRiver
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndtertDto
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRepository
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRiver
import no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet.VedtaksperiodeForkastetDto
import no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet.VedtaksperiodeForkastetRiver
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.jetbrains.exposed.sql.Database as ExposedDatabase

private val logger: Logger = LoggerFactory.getLogger("sparkel-arbeidsgiver")

fun main() {
    val env = System.getenv()

    val database = Database()
    val db = ExposedDatabase.connect(database.dataSource)
    val inntektsmeldingRegistrertRepository = InntektsmeldingRegistrertRepository(db)

    val trengerForespørselProducer = createAivenProducer<TrengerArbeidsgiveropplysningerDto>(env)
    val trengerIkkeForespørselProducer = createAivenProducer<TrengerIkkeArbeidsgiveropplysningerDto>(env)
    val inntektsmeldingHåndtertProducer = createAivenProducer<InntektsmeldingHåndtertDto>(env)
    val vedtaksperiodeForkastetProducer = createAivenProducer<VedtaksperiodeForkastetDto>(env)

    val app = RapidApplication.create(env).apply {
        registerDbLifecycle(database)
        TrengerArbeidsgiveropplysningerRiver(this, trengerForespørselProducer)
        TrengerIkkeArbeidsgiveropplysningerRiver(this, trengerIkkeForespørselProducer)
        TrengerArbeidsgiveropplysningerBegrensetRiver(this, trengerForespørselProducer)
        InntektsmeldingHåndertRiver(this, inntektsmeldingHåndtertProducer, inntektsmeldingRegistrertRepository)
        InntektsmeldingRegistrertRiver(this, inntektsmeldingRegistrertRepository)
        VedtaksperiodeForkastetRiver(this, vedtaksperiodeForkastetProducer)
    }
    logger.info("Hei, bro!")
    app.start()
}

private fun RapidsConnection.registerDbLifecycle(db: Database) {
    register(object : RapidsConnection.StatusListener {
        override fun onStartup(rapidsConnection: RapidsConnection) {
            db.migrate()
        }

        override fun onShutdown(rapidsConnection: RapidsConnection) {
            db.dataSource.close()
        }
    })
}

private fun <T> createAivenProducer(env: Map<String, String>): KafkaProducer<String, T> {
    val properties = Properties().apply {
        put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.getValue("KAFKA_BROKERS"))
        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
        put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
        put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
        put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
        put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, env.getValue("KAFKA_TRUSTSTORE_PATH"))
        put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, env.getValue("KAFKA_CREDSTORE_PASSWORD"))
        put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, env.getValue("KAFKA_KEYSTORE_PATH"))
        put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, env.getValue("KAFKA_CREDSTORE_PASSWORD"))

        put(ProducerConfig.ACKS_CONFIG, "1")
        put(ProducerConfig.LINGER_MS_CONFIG, "0")
        put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
    }
    return KafkaProducer(properties, StringSerializer(), CustomSerializer<T>())
}

class CustomSerializer<T> : Serializer<T> {
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModules(JavaTimeModule())

    override fun serialize(topic: String, data: T): ByteArray = objectMapper.writeValueAsBytes(data)
}
