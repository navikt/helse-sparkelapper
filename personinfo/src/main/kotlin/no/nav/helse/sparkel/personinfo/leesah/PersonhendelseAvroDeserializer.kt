package no.nav.helse.sparkel.personinfo.leesah

import java.net.URL
import java.util.Base64
import no.nav.helse.sparkel.personinfo.leesah.Skjemainnhenter.resourceUrl
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
            return deserialize(data, lokaltSkjema.schema)
        } catch (exception: Exception) {
            sikkerlogg.feilVedDeserialisering(data, exception, lokaltSkjema.versjon)
            if (lokaltSkjema == sisteSkjema) {
                logger.error("Deserialiseringsfeil tross at vi er på siste versjon ${sisteSkjema.versjon}")
                throw exception
            }
        }

        try {
            val record = deserialize(data, sisteSkjema.schema)
            logger.error("Deserialiserte melding med siste versjon av skjemaet (${sisteSkjema.versjon}). Vi er på versjon ${lokaltSkjema.versjon}. Burde oppdateres!")
            return record
        } catch (exception: Exception) {
            logger.error("Deserialiseringsfeil også på siste versjon ${sisteSkjema.versjon}")
            sikkerlogg.feilVedDeserialisering(data, exception, sisteSkjema.versjon)
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
        private val logger = LoggerFactory.getLogger(PersonhendelseAvroDeserializer::class.java)
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private fun Logger.feilVedDeserialisering(data: ByteArray, throwable: Throwable, versjon: String) =
            warn("Klarte ikke å deserialisere Personhendelse-melding fra Leesah med versjon $versjon. Base64='${Base64.getEncoder().encodeToString(data)}'", throwable)
        internal val lokaltSkjema = Skjemainnhenter.hentSkjema("/pdl/Personhendelse_V11.avsc".resourceUrl()) { "11" }
        private val sisteSkjema by lazy {
            System.getenv("SISTE_PERSONHENDELSE_SKJEMA_URL")?.let { Skjemainnhenter.hentSkjema(URL(it)) } ?: lokaltSkjema
        }
    }
}
