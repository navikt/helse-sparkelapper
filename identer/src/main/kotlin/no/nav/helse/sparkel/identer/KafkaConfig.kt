package no.nav.helse.sparkel.identer

import java.util.Properties
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer

private fun loadBaseConfig(): Properties = Properties().also {
    val username = System.getenv("sparkelidenter_username")
    val password = System.getenv("sparkelidenter_password")
    it["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"$username\" password=\"$password\";"
    it["bootstrap.servers"] = System.getenv("KAFKA_BOOTSTRAP_SERVERS")
    it["specific.avro.reader"] = true
    it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
    it[SaslConfigs.SASL_MECHANISM] = "PLAIN"
    it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = System.getenv("NAV_TRUSTSTORE_PATH")
    it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("NAV_TRUSTSTORE_PASSWORD")
    // consumer specifics:
    it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    it[ConsumerConfig.GROUP_ID_CONFIG] = "sparkel-identer-v1"
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = AktørAvroDeserializer::class.java
    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "1"
}

fun createConsumer() = KafkaConsumer<ByteArray, GenericRecord>(loadBaseConfig())