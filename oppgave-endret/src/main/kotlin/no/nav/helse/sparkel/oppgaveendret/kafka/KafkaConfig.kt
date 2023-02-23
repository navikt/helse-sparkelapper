package no.nav.helse.sparkel.oppgaveendret.kafka

import java.util.Properties
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer

private fun loadBaseConfig(): Properties = Properties().also {
    it["bootstrap.servers"] = System.getenv("KAFKA_BROKERS")
    it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
    it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = System.getenv("KAFKA_KEYSTORE_PATH")
    it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
    it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = System.getenv("KAFKA_TRUSTSTORE_PATH")
    it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
    // consumer specifics:
    it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"
    it[ConsumerConfig.GROUP_ID_CONFIG] = "sparkel-oppgave-endret-v1"
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class
    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "5000"
    it[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG] = "5242880"
    it[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 86400000
}

fun createConsumer() = KafkaConsumer<String, String>(loadBaseConfig())