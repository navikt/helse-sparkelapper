package no.nav.helse.sparkel.oppgaveendret.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.*
import java.io.File
import java.util.Properties
import no.nav.helse.sparkel.oppgaveendret.util.ServiceUser
import kotlin.reflect.KClass

data class KafkaConfig(
    val kafkaBootstrapServers: String,
    val truststore: String?,
    val truststorePassword: String?,
    val cluster: String
)

fun loadBaseConfig(env: KafkaConfig, serviceUser: ServiceUser): Properties = Properties().also {
    it.load(KafkaConfig::class.java.getResourceAsStream("/kafka_base.properties"))
    it["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"${serviceUser.username}\" password=\"${serviceUser.password}\";"
    it["bootstrap.servers"] = env.kafkaBootstrapServers
    if (env.cluster != "localhost") {
        it[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
        it[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = File(env.truststore!!).absolutePath
        it[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = env.truststorePassword!!
    }
}

fun Properties.toConsumerConfig(
    groupId: String,
    valueDeserializer: KClass<out Deserializer<out Any>>,
    keyDeserializer: KClass<out Deserializer<out Any>> = StringDeserializer::class
): Properties = Properties().also {
    it.putAll(this)
    it[ConsumerConfig.GROUP_ID_CONFIG] = groupId
    it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = keyDeserializer.java
    it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = valueDeserializer.java
    it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "5000"
    it[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG] = "5242880"
}
