package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.nyttDokument
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class PersonhendelseAvroDeserializerTest {
    val encoderFactory = EncoderFactory.get()
    @Test
    fun `klarer å parse personhendelsedokument fra leesah`() {
        val dokument = nyttDokument("20046913337")
        assertEquals(dokument, PersonhendelseAvroDeserializer().deserialize("leesah", serialize(dokument)))
    }

    private fun serialize(record: GenericRecord): ByteArray {
        val writer = GenericDatumWriter<GenericRecord>(record.schema)
        val bytesOut = ByteArrayOutputStream()
        val encoder = encoderFactory.binaryEncoder(bytesOut, null)
        writer.write(record, encoder)
        encoder.flush()
        // KafkaAvroSerializer legger på to magic bytes, vet ikke hva de er
        return byteArrayOf(0, 0) + bytesOut.toByteArray()
    }
}
