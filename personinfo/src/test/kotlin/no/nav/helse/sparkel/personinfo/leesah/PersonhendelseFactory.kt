package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import java.util.*

object PersonhendelseFactory {

    fun nyttDokument(
        fodselsnummer: String,
        gradering: PersonhendelseOversetter.Gradering = PersonhendelseOversetter.Gradering.UGRADERT,
        opplysningstype: String = "ADRESSEBESKYTTELSE_V1"
    ): GenericRecord = GenericData.Record(PersonhendelseAvroDeserializer.schema).apply {
        val addressebeskyttelseSchema =
            PersonhendelseAvroDeserializer.schema.getField("adressebeskyttelse").schema().types.last()
        put("opplysningstype", opplysningstype)
        put("hendelseId", UUID.randomUUID().toString())
        put("personidenter", listOf(fodselsnummer))
        put("master", "skatt")
        put("opprettet", 420L)
        put("endringstype", GenericData.EnumSymbol(PersonhendelseAvroDeserializer.schema, "KORRIGERT"))
        put("adressebeskyttelse", GenericData.Record(addressebeskyttelseSchema).apply {
            put("gradering", GenericData.EnumSymbol(addressebeskyttelseSchema, gradering.name))
        })
    }
}