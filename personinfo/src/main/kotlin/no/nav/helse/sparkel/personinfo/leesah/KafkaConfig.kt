package no.nav.helse.sparkel.personinfo.leesah

import java.util.Properties
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer

private fun loadBaseConfig(): Properties = Properties().also {
    it["bootstrap.servers"] = System.getenv("KAFKA_BROKERS")
    it["specific.avro.reader"] = true
    it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
    it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = System.getenv("KAFKA_TRUSTSTORE_PATH")
    it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
    it[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = System.getenv("KAFKA_KEYSTORE_PATH")
    it[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
    // consumer specifics:
    it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "none"
    it[ConsumerConfig.GROUP_ID_CONFIG] = "sparkel-personinfo-v1"
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = PersonhendelseAvroDeserializer::class.java
    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "200"
}

fun createConsumer() = KafkaConsumer<ByteArray, GenericRecord>(loadBaseConfig())
