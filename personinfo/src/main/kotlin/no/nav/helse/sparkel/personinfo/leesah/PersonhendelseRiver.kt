package no.nav.helse.sparkel.personinfo.leesah

import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory

class PersonhendelseRiver {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    fun onPackage(record: GenericRecord) {
        if (record.get("opplysningstype") != "ADRESSEBESKYTTELSE_V1") return
        sikkerlogg.info("mottok endring på adressebeskyttelse")
    }
}
