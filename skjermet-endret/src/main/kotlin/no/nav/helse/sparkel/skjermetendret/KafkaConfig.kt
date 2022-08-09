package no.nav.helse.sparkel.skjermetendret

import java.util.Properties
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer

val SKJERMET_TOPIC = "nom.skjermede-personer-status-v1"

private fun loadBaseConfig(): Properties = Properties().also {
    it[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = System.getenv("KAFKA_BROKERS")
    it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SecurityProtocol.SSL.name

    it[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
    it[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "jks"
    it[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
    it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = System.getenv("KAFKA_TRUSTSTORE_PATH")
    it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
    it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = System.getenv("KAFKA_KEYSTORE_PATH")
    it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
    it[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")

    it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    it[ConsumerConfig.GROUP_ID_CONFIG] = "sparkel-skjermet-endret-dev-v1"
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "100"
}

fun createConsumer() = KafkaConsumer<String, String>(loadBaseConfig()).also {
    it.subscribe(listOf(SKJERMET_TOPIC))
}