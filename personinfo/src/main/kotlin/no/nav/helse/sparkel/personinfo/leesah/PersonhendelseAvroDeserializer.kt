package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.avro.util.Utf8
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory

class PersonhendelseAvroDeserializer : Deserializer<GenericRecord> {
    private val decoderFactory: DecoderFactory = DecoderFactory.get()
    override fun deserialize(topic: String, data: ByteArray): GenericRecord {
        try {
            val reader = GenericDatumReader<GenericRecord>(schema)
            val decoder = decoderFactory.binaryDecoder(data, null)
            /*
            KafkaAvroSerializer legger på 5 bytes, 1 magic byte og 4 som sier noe om hvilke entry i schema registeret som
            brukes. Siden vi ikke ønsker å ha et dependency til schema registryet har vi en egen deserializer og skipper de
            5 første bytene
             */
            decoder.skipFixed(5)
            return reader.read(null, decoder)
        } catch (throwable: Throwable) {
            sikkerlogg.warn("Mottok ugyldig melding fra Leesah '${Utf8(data)}'", throwable)
            throw throwable
        }
    }

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val schemaVersjon = System.getenv("PERSONHENDELSE_VERSJON") ?: "V11"
        val schema = Schema.Parser().parse(PersonhendelseAvroDeserializer::class.java.getResourceAsStream("/pdl/Personhendelse_$schemaVersjon.avsc"))
    }
}
