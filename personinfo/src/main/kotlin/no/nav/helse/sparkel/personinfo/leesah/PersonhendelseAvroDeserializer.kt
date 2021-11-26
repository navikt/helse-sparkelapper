package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.kafka.common.serialization.Deserializer

class PersonhendelseAvroDeserializer : Deserializer<GenericRecord> {
    private val decoderFactory: DecoderFactory = DecoderFactory.get()
    override fun deserialize(topic: String, data: ByteArray): GenericRecord {
        val reader = GenericDatumReader<GenericRecord>(schema)
        val decoder = decoderFactory.binaryDecoder(data, null)
        decoder.skipFixed(2)
        return reader.read(null, decoder)
    }

    companion object {
        val schema = Schema.Parser().parse(PersonhendelseAvroDeserializer::class.java.getResourceAsStream("/pdl/PersonhendelseProto.avsc"))
    }
}
