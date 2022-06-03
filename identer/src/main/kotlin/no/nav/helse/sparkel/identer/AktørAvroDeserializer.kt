package no.nav.helse.sparkel.identer

import java.util.Base64
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory

class AktørAvroDeserializer : Deserializer<GenericRecord> {
    private val decoderFactory: DecoderFactory = DecoderFactory.get()

    override fun deserialize(topic: String, data: ByteArray): GenericRecord {
        try {
            return deserialize(data, schema)
        } catch (exception: Exception) {
            sikkerlogg.warn("Klarte ikke å deserialisere melding. Base64='${Base64.getEncoder().encodeToString(data)}'", exception)
            throw exception
        }
    }

    private fun deserialize(data: ByteArray, schema: Schema) : GenericRecord {
        val reader = GenericDatumReader<GenericRecord>(schema)
        val decoder = decoderFactory.binaryDecoder(data, null)
        /*
        KafkaAvroSerializer legger på 5 bytes, 1 magic byte og 4 som sier noe om hvilke entry i schema registeret som
        brukes. Siden vi ikke ønsker å ha et dependency til schema registryet har vi en egen deserializer og skipper de
        5 første bytene
         */
        decoder.skipFixed(5)
        return reader.read(null, decoder)
    }

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val schema =
            Schema.Parser().parse(AktørAvroDeserializer::class.java.getResourceAsStream("/pdl/AktorV2.avsc"))
    }
}
