package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseAvroDeserializer.Companion.sisteSkjema

/**
 * Oppretter testdata på siste/nyeste versjon av skjemaet
 */
internal object PersonhendelseFactory {
    private val encoderFactory = EncoderFactory.get()

    internal fun adressebeskyttelse(
        fodselsnummer: String,
        gradering: PersonhendelseOversetter.Gradering = PersonhendelseOversetter.Gradering.UGRADERT,
        hendelseId: UUID = UUID.randomUUID()
    ): GenericRecord = GenericData.Record(sisteSkjema).apply {
        val addressebeskyttelseSchema = sisteSkjema.getField("adressebeskyttelse").schema().types.last()
        val deltBostedSchema = sisteSkjema.getField("deltBosted").schema().types.last()
        put("opplysningstype", "ADRESSEBESKYTTELSE_V1")
        put("hendelseId", "$hendelseId")
        put("personidenter", listOf(fodselsnummer))
        put("master", "skatt")
        put("opprettet", 420L)
        put("falskIdentitet", null)
        put("deltBosted", GenericData.Record(deltBostedSchema).apply { put("startdatoForKontrakt", 1234) })
        put("endringstype", GenericData.EnumSymbol(sisteSkjema, "KORRIGERT"))
        put("adressebeskyttelse", GenericData.Record(addressebeskyttelseSchema).apply {
            put("gradering", GenericData.EnumSymbol(addressebeskyttelseSchema, gradering.name))
        })
    }

    internal fun navn(
        vararg fodselsnumre: String,
        hendelseId: UUID,
    ): GenericRecord = GenericData.Record(sisteSkjema).apply {
        val navnSkjema = sisteSkjema.getField("navn").schema().types.last()
        put("opplysningstype", "NAVN_V1")
        put("hendelseId", "$hendelseId")
        put("personidenter", listOf(*fodselsnumre))
        put("master", "FREG")
        put("opprettet", 1758870774932)
        put("endringstype", GenericData.EnumSymbol(sisteSkjema, "OPPRETTET"))
        put("navn", GenericData.Record(navnSkjema).apply {
            put("fornavn", "SKY")
            put("etternavn", "SKJORTE")
            put("gyldigFraOgMed", 20216)
        })
    }

    internal fun dødsfall(
        fodselsnummer: String,
        dødsdato: LocalDate,
        hendelseId: UUID = UUID.randomUUID()
    ): GenericRecord = GenericData.Record(sisteSkjema).apply {
        val schema = sisteSkjema.getField("doedsfall").schema().types.last()
        put("opplysningstype", "DOEDSFALL_V1")
        put("hendelseId", "$hendelseId")
        put("personidenter", listOf(fodselsnummer))
        put("master", "skatt")
        put("opprettet", 420L)
        put("endringstype", GenericData.EnumSymbol(sisteSkjema, "KORRIGERT"))
        put("doedsfall", GenericData.Record(schema).apply {
            put("doedsdato", dødsdato.toEpochDay())
        })
    }

    internal fun folkeregisteridentifikator(
        fodselsnummer: String, hendelseId: UUID = UUID.randomUUID()
    ): GenericRecord = GenericData.Record(sisteSkjema).apply {
        put("opplysningstype", "FOLKEREGISTERIDENTIFIKATOR_V1")
        put("hendelseId", "$hendelseId")
        put("personidenter", listOf(fodselsnummer))
        put("master", "skatt")
        put("opprettet", 420L)
        put("endringstype", GenericData.EnumSymbol(sisteSkjema, "KORRIGERT"))
        val schema = sisteSkjema.getField("Folkeregisteridentifikator").schema().types.last()
        put("Folkeregisteridentifikator", GenericData.Record(schema).apply {
            put("identifikasjonsnummer", fodselsnummer)
            put("type", "FNR")
            put("status", "opphoert")
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
