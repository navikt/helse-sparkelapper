package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.rapids_rivers.KafkaConfig
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private fun loadBaseConfig(): Properties = Properties().also {
    val username = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/username"))
    val password = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/password"))
    it.load(KafkaConfig::class.java.getResourceAsStream("/kafka_base.properties"))
    it["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"$username\" password=\"$password\";"
    it["bootstrap.servers"] = System.getenv("KAFKA_BOOTSTRAP_SERVERS")
    it["specific.avro.reader"] = true
    it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
    it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = System.getenv("KAFKA_TRUSTSTORE_PATH")
    it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("KAFKA_CREDSTORE_PASSWORD")
    // consumer specifics:
    it[ConsumerConfig.GROUP_ID_CONFIG] = "sparkel-personinfo"
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = PersonhendelseAvroDeserializer
    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "1"
}

fun createConsumer() = KafkaConsumer<ByteArray, GenericRecord>(loadBaseConfig())