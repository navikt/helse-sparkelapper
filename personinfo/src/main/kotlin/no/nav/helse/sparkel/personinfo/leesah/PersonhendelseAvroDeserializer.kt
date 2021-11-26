package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory
import java.util.*

class PersonhendelseAvroDeserializer : Deserializer<GenericRecord> {
    val decoderFactory = DecoderFactory.get()
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    override fun deserialize(topic: String, data: ByteArray): GenericRecord {
        sikkerlogg.info("avro melding ${Base64.getEncoder().encodeToString(data)}")
        val reader = GenericDatumReader<GenericRecord>(schema)
        return reader.read(null, decoderFactory.binaryDecoder(data, null))
    }

    companion object {
        val schema = Schema.Parser().parse(PersonhendelseAvroDeserializer::class.java.getResourceAsStream("/pdl/PersonhendelseProto.avsc"))
    }
}
