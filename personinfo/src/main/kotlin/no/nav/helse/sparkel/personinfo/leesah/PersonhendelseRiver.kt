package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory

class PersonhendelseRiver {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    fun onPackage(record: GenericRecord) {
        if (record.get("opplysningstype") != "ADRESSEBESKYTTELSE_V1") {
            sikkerlogg.info("Mottok event på ident ${record.get("personidenter")}")
            return
        }
        if (finnGradering(record) == PersonhendelseOversetter.Gradering.UGRADERT.name) {
            sikkerlogg.info("mottok endring på adressebeskyttelse: ${record}")
        }
    }

    private fun finnGradering(record: GenericRecord) =
        (record.get("adressebeskyttelse") as GenericRecord).get("gradering").toString()
}








