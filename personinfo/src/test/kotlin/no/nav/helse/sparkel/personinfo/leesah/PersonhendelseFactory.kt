package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.UUID

internal object PersonhendelseFactory {
    private val encoderFactory = EncoderFactory.get()

    internal fun adressebeskyttelseV1(
        fodselsnummer: String,
        gradering: PersonhendelseOversetter.Gradering = PersonhendelseOversetter.Gradering.UGRADERT,
        hendelseId: UUID = UUID.randomUUID()
    ): GenericRecord = GenericData.Record(PersonhendelseAvroDeserializer.sisteSkjema).apply {
        val addressebeskyttelseSchema =
            PersonhendelseAvroDeserializer.sisteSkjema.getField("adressebeskyttelse").schema().types.last()
        put("opplysningstype", "ADRESSEBESKYTTELSE_V1")
        put("hendelseId", "$hendelseId")
        put("personidenter", listOf(fodselsnummer))
        put("master", "skatt")
        put("opprettet", 420L)
        put("endringstype", GenericData.EnumSymbol(PersonhendelseAvroDeserializer.sisteSkjema, "KORRIGERT"))
        put("adressebeskyttelse", GenericData.Record(addressebeskyttelseSchema).apply {
            put("gradering", GenericData.EnumSymbol(addressebeskyttelseSchema, gradering.name))
        })
    }

    internal fun dødsfallV1(
        fodselsnummer: String,
        dødsdato: LocalDate,
        hendelseId: UUID = UUID.randomUUID()
    ): GenericRecord = GenericData.Record(PersonhendelseAvroDeserializer.sisteSkjema).apply {
        val schema =
            PersonhendelseAvroDeserializer.sisteSkjema.getField("doedsfall").schema().types.last()
        put("opplysningstype", "DOEDSFALL_V1")
        put("hendelseId", "$hendelseId")
        put("personidenter", listOf(fodselsnummer))
        put("master", "skatt")
        put("opprettet", 420L)
        put("endringstype", GenericData.EnumSymbol(PersonhendelseAvroDeserializer.sisteSkjema, "KORRIGERT"))
        put("doedsfall", GenericData.Record(schema).apply {
            put("doedsdato", dødsdato.toEpochDay())
        })
    }

    internal fun serialize(record: GenericRecord): ByteArray {
        val writer = GenericDatumWriter<GenericRecord>(record.schema)
        val bytesOut = ByteArrayOutputStream()
        val encoder = encoderFactory.binaryEncoder(bytesOut, null)
        writer.write(record, encoder)
        encoder.flush()
        // KafkaAvroSerializer legger på fem magic bytes
        // https://docs.confluent.io/3.2.0/schema-registry/docs/serializer-formatter.html#wire-format
        return byteArrayOf(0, 0, 0, 0, 0) + bytesOut.toByteArray()
    }
}
