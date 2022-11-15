package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.personinfo.PdlClient
import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.sparkel.personinfo.FantIkkeIdenter
import no.nav.helse.sparkel.personinfo.Identer
import no.nav.helse.sparkel.personinfo.IdenterResultat

internal class PersonhendelseRiver(
    private val rapidsConnection: RapidsConnection,
    private val pdlClient: PdlClient,
    private val cacheTimeout: Duration
) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private var forrigeIdent: String? = null
    private var forrigeOppdatering = LocalDateTime.MIN

    fun onPackage(record: GenericRecord) {
        if (record.get("opplysningstype").toString() != "ADRESSEBESKYTTELSE_V1") return
        sikkerlogg.info("mottok endring på adressebeskyttelse")

            val ident: String = (record.get("personidenter") as List<Any?>).firstOrNull().takeIf { it != null }?.toString()
            ?: run {
                sikkerlogg.warn("Forventet feltet personidenter i meldingen\n$record")
                throw RuntimeException("no gæernt")
            }
        if (throttle(ident)) return

        when (val identer: IdenterResultat = pdlClient.hentIdenter(ident, UUID.randomUUID().toString())) {
            is Identer -> sendMelding(identer)
            is FantIkkeIdenter -> sikkerlogg.info("Kan ikke registrere addressebeskyttelse-endring på $ident pga manglende fnr")
        }
    }

    private fun sendMelding(identer: Identer) {
        val packet: JsonMessage = JsonMessage.newMessage(
            mapOf(
                "@event_name" to "adressebeskyttelse_endret",
                "@id" to UUID.randomUUID(),
                "@opprettet" to LocalDateTime.now(),
                "fødselsnummer" to identer.fødselsnummer,
                "aktørId" to identer.aktørId
            )
        )

        rapidsConnection.publish(identer.fødselsnummer, packet.toJson())
    }

    private fun throttle(ident: String): Boolean {
        if (ident == forrigeIdent && forrigeOppdatering.plusNanos(cacheTimeout.toNanos()) > LocalDateTime.now()) return true
        forrigeIdent = ident
        forrigeOppdatering = LocalDateTime.now()
        return false
    }
}
