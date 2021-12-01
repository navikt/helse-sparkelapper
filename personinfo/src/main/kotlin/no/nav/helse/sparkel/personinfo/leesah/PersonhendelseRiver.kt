package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.personinfo.PdlClient
import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory

internal class PersonhendelseRiver(private val rapidsConnection: RapidsConnection, private val pdlClient: PdlClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    fun onPackage(record: GenericRecord) {
        if (record.get("opplysningstype") != "ADRESSEBESKYTTELSE_V1") return
        sikkerlogg.info("mottok endring på adressebeskyttelse")
    }
}
