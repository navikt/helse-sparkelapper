package no.nav.helse.sparkel.oppgaveendret

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.kafka.KafkaConfig
import no.nav.helse.sparkel.oppgaveendret.kafka.loadBaseConfig
import no.nav.helse.sparkel.oppgaveendret.kafka.toConsumerConfig
import no.nav.helse.sparkel.oppgaveendret.pdl.PdlClient
import no.nav.helse.sparkel.oppgaveendret.sts.StsRestClient
import no.nav.helse.sparkel.oppgaveendret.util.ServiceUser
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {

    val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val serviceUser = "/var/run/secrets/nais.io/service_user".let {
        ServiceUser(
            "$it/username".readFile(),
            "$it/password".readFile()
        )
    }

    val stsClient = StsRestClient(
        baseUrl = env.getValue("STS_BASE_URL"),
        serviceUser = serviceUser,
        objectMapper = objectMapper
    )
    val pdlClient = PdlClient(
        baseUrl = env.getValue("PDL_URL"),
        stsClient = stsClient,
        objectMapper = objectMapper
    )
    val kafkaConfig = KafkaConfig(
        kafkaBootstrapServers = getEnvVar(env,"KAFKA_BOOTSTRAP_SERVERS_URL"),
        truststore = getEnvVar(env,"NAV_TRUSTSTORE_PATH"),
        truststorePassword = getEnvVar(env,"NAV_TRUSTSTORE_PASSWORD"),
        cluster = getEnvVar(env,"NAIS_CLUSTER_NAME")
    )
    val properties = loadBaseConfig(kafkaConfig, serviceUser)
    val applicationName: String = getEnvVar(env,"NAIS_APP_NAME")
    val consumerProperties =
        properties.toConsumerConfig("${applicationName}-consumer", valueDeserializer = StringDeserializer::class)
    val consumeTopic = getEnvVar(env,"OPPGAVE_ENDRET_TOPIC")

    val kafkaConsumerOppgaveEndret = KafkaConsumer<String, String>(consumerProperties)
    kafkaConsumerOppgaveEndret.subscribe(listOf(consumeTopic))

    return RapidApplication.create(env).apply {
        val oppgaveEndretProducer = OppgaveEndretProducer(this, pdlClient)
        val oppgaveEndretConsumer = OppgaveEndretConsumer(this, kafkaConsumerOppgaveEndret, oppgaveEndretProducer, objectMapper)
        Thread(oppgaveEndretConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                oppgaveEndretConsumer.close()
            }
        })
    }
}

private fun getEnvVar(env: Map<String, String>, varName: String) =
    env[varName] ?: throw RuntimeException("Missing required variable \"$varName\"")


private fun String.readFile() = File(this).readText(Charsets.UTF_8)
