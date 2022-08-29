package no.nav.helse.sparkel.oppgaveendret

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.time.Clock
import java.util.Properties
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.kafka.KafkaConfig
import no.nav.helse.sparkel.oppgaveendret.kafka.loadBaseConfig
import no.nav.helse.sparkel.oppgaveendret.kafka.toConsumerConfig
import no.nav.helse.sparkel.oppgaveendret.oppgave.OppgaveEndretConsumer
import no.nav.helse.sparkel.oppgaveendret.oppgave.OppgaveEndretSniffer
import no.nav.helse.sparkel.oppgaveendret.util.ServiceUser
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory


fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal val objectMapper: ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerKotlinModule()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

internal fun createApp(env: Map<String, String>): RapidsConnection {

    val serviceUser = "/var/run/secrets/nais.io/service_user".let {
        ServiceUser(
            "$it/username".readFile(),
            "$it/password".readFile()
        )
    }

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

    val kafkaConsumerForSniffing = lagSnifferConsumer(properties, consumeTopic)

    return RapidApplication.create(env).apply {
        val gosysOppgaveEndretProducer = GosysOppgaveEndretProducer(this)
        val oppgaveEndretConsumer = OppgaveEndretConsumer(
            this,
            kafkaConsumerOppgaveEndret,
            gosysOppgaveEndretProducer,
            objectMapper,
            Clock.systemDefaultZone(),
        )
        Thread(oppgaveEndretConsumer).start()
        spolSniffernTilStart(kafkaConsumerForSniffing, consumeTopic)
//        val oppgaveEndretSniffer = OppgaveEndretSniffer(kafkaConsumerForSniffing, objectMapper)
//        Thread(oppgaveEndretSniffer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                oppgaveEndretConsumer.close()
                kafkaConsumerForSniffing.close()
            }
        })
    }
}

private fun lagSnifferConsumer(
    properties: Properties,
    consumeTopic: String
): KafkaConsumer<String, String> {
    val snifferKafkaConsumer = KafkaConsumer<String, String>(
        properties.toConsumerConfig("oppgave-endret-sniffer", valueDeserializer = StringDeserializer::class).also {
            it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        })
    snifferKafkaConsumer.subscribe(listOf(consumeTopic))
    return snifferKafkaConsumer
}

fun spolSniffernTilStart(consumer: KafkaConsumer<String, String>, topic: String) {
    val log = LoggerFactory.getLogger("sniffespoler")
    consumer.subscribe(listOf(topic), object : ConsumerRebalanceListener {
        override fun onPartitionsRevoked(partitions: Collection<TopicPartition>) {}
        override fun onPartitionsAssigned(partitions: Collection<TopicPartition>) {
            val topicAndOffsets = consumer.committed(partitions.toSet())
            for ((topicPartition, offset) in topicAndOffsets) {
                log.info("Current offset for partition ${topicPartition.partition()} is $offset")
            }

            log.info("Partitions assigned: $partitions")
            for (tp in partitions) {
                val oam: OffsetAndMetadata? = consumer.committed(tp)
                if (oam != null) {
                    log.info("Current offset for ${oam.metadata()} is ${oam.offset()}")
                } else {
                    log.info("No committed offsets")
                }
            }
            consumer.seekToBeginning(partitions)
        }
    })
}

private fun getEnvVar(env: Map<String, String>, varName: String) =
    env[varName] ?: throw RuntimeException("Missing required variable \"$varName\"")


private fun String.readFile() = File(this).readText(Charsets.UTF_8)
