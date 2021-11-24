package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.*

class PersonhendelseAvroDeserializerTest {
    val encoderFactory = EncoderFactory.get()
    @Test
    fun `klarer å parse personhendelsedokument fra leesah`() {
        val dokument = nyttDokument("20046913337")
        assertEquals(dokument, PersonhendelseAvroDeserializer().deserialize("leesah", serialize(dokument)))
    }

    fun nyttDokument(
        fodselsnummer: String,
        gradering: PersonhendelseOversetter.Gradering = PersonhendelseOversetter.Gradering.UGRADERT,
    ): GenericRecord = GenericData.Record(PersonhendelseAvroDeserializer.schema).apply {
        val addressebeskyttelseSchema =
            PersonhendelseAvroDeserializer.schema.getField("adressebeskyttelse").schema().types.last()
        put("opplysningstype", "ADRESSEBESKYTTELSE_V1")
        put("hendelseId", UUID.randomUUID().toString())
        put("personidenter", listOf(fodselsnummer))
        put("master", "skatt")
        put("opprettet", 420L)
        put("endringstype", GenericData.EnumSymbol(PersonhendelseAvroDeserializer.schema, "KORRIGERT"))
        put("adressebeskyttelse", GenericData.Record(addressebeskyttelseSchema).apply {
            put("gradering", GenericData.EnumSymbol(addressebeskyttelseSchema, gradering.name))
        })
    }

    private fun serialize(record: GenericRecord): ByteArray {
        val writer = GenericDatumWriter<GenericRecord>(record.schema)
        val bytesOut = ByteArrayOutputStream()
        val encoder = encoderFactory.binaryEncoder(bytesOut, null)
        writer.write(record, encoder)
        encoder.flush()
        return bytesOut.toByteArray()
    }
}
