package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private fun loadBaseConfig(): Properties = Properties().also {
    val username = System.getenv("srvsparkelpersoninfo_username")
    val password = System.getenv("srvsparkelpersoninfo_password")
    it["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"$username\" password=\"$password\";"
    it["bootstrap.servers"] = System.getenv("KAFKA_BOOTSTRAP_SERVERS")
    it["specific.avro.reader"] = true
    it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
    it[SaslConfigs.SASL_MECHANISM] = "PLAIN"
    it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = System.getenv("NAV_TRUSTSTORE_PATH")
    it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("NAV_TRUSTSTORE_PASSWORD")
    // consumer specifics:
    it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"
    it[ConsumerConfig.GROUP_ID_CONFIG] = "sparkel-personinfo"
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = PersonhendelseAvroDeserializer::class.java
    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "200"
}

fun createConsumer() = KafkaConsumer<ByteArray, GenericRecord>(loadBaseConfig())
