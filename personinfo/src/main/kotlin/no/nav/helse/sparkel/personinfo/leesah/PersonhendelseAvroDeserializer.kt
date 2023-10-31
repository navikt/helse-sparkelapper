package no.nav.helse.sparkel.personinfo.leesah

import java.util.Base64
import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PersonhendelseAvroDeserializer : Deserializer<GenericRecord> {
    private val decoderFactory: DecoderFactory = DecoderFactory.get()

    override fun deserialize(topic: String, data: ByteArray): GenericRecord {
        try {
            return deserialize(data, v13Skjema)
        } catch (exception: Exception) {
            sikkerlogg.feilVedDeserialisering(data, exception, "V13")
        }

        try {
            return deserialize(data, v12Skjema)
        } catch (exception: Exception) {
            sikkerlogg.feilVedDeserialisering(data, exception, "V12")
        }

        try {
            return deserialize(data, v11Skjema)
        } catch (exception: Exception) {
            sikkerlogg.feilVedDeserialisering(data, exception, "V11")
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
        private fun Logger.feilVedDeserialisering(data: ByteArray, throwable: Throwable, versjon: String) =
            warn("Klarte ikke å deserialisere Personhendelse-melding fra Leesah med $versjon. Base64='${Base64.getEncoder().encodeToString(data)}'", throwable)
        private fun String.lastSkjema() =
            Schema.Parser().parse(PersonhendelseAvroDeserializer::class.java.getResourceAsStream("/pdl/Personhendelse_$this.avsc"))
        private val v11Skjema = "V11".lastSkjema()
        private val v12Skjema = "V12".lastSkjema()
        private val v13Skjema = "V13".lastSkjema()
        val sisteSkjema = v13Skjema
    }
}
