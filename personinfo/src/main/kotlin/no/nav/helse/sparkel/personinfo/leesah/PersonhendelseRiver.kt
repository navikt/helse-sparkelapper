package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.personinfo.PdlClient
import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.*
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.personinfo.FantIkkeIdenter
import no.nav.helse.sparkel.personinfo.Identer
import no.nav.helse.sparkel.personinfo.IdenterResultat
import org.apache.avro.generic.GenericData

internal class PersonhendelseRiver(
    private val rapidsConnection: RapidsConnection,
    private val pdlClient: PdlClient,
    private val cacheTimeout: Duration
) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private var forrigeIdent: String? = null
    private var forrigeOppdatering = LocalDateTime.MIN

    fun onPackage(record: GenericRecord) {
        val opplysningstype = record.get("opplysningstype").toString()
        when (opplysningstype) {
            "ADRESSEBESKYTTELSE_V1" -> håndterAdressebeskyttelse(record)
            "DOEDSFALL_V1" -> håndterDødsmelding(record)
            "FOLKEREGISTERIDENTIFIKATOR_V1" -> håndterFolkeregisteridentifikator(record)
            else -> sikkerlogg.info("uhåndtert melding {}\n$record", keyValue("opplysningstype", opplysningstype))
        }
    }

    private fun håndterDødsmelding(record: GenericRecord) {
        sikkerlogg.info("mottok melding om dødsfall: $record")

        val dødsfall = record.get("doedsfall")
        if (dødsfall !is GenericData.Record) return
        val dødsdato = dødsfall["doedsdato"] ?: return
        val dato = try {
            LocalDate.ofEpochDay("$dødsdato".toLong())
        } catch (err: DateTimeParseException) {
            return sikkerlogg.info("dødsdato <$dødsdato> er ikke en dato: ${err.message}\nRecord: $record", err)
        }

        val ident = (record.get("personidenter") as List<Any?>).first().toString()
        val identer: IdenterResultat = pdlClient.hentIdenter(ident, UUID.randomUUID().toString())
        if (identer !is Identer) return sikkerlogg.info("Kan ikke registrere dødsdato-endring på $ident pga manglende fnr")
        val packet = JsonMessage.newMessage(mapOf(
            "@event_name" to "dødsmelding",
            "@id" to UUID.randomUUID(),
            "@opprettet" to LocalDateTime.now(),
            "fødselsnummer" to identer.fødselsnummer,
            "aktørId" to identer.aktørId,
            "dødsdato" to "$dato",
            "lesahHendelseId" to "${record.get("hendelseId")}"
        ))
        sikkerlogg.info("publiserer dødsmelding for {} {}:\n${packet.toJson()}",
            keyValue("fødselsnummer", identer.fødselsnummer),
            keyValue("aktørId", identer.aktørId)
        )
        rapidsConnection.publish(identer.fødselsnummer, packet.toJson())
    }

    private fun håndterFolkeregisteridentifikator(record: GenericRecord) {
        sikkerlogg.info("mottok melding om folkeregisteridentifikator:\n$record")

        val folkeregisteridentifikator = record.get("Folkeregisteridentifikator")
        if (folkeregisteridentifikator !is GenericData.Record) return
        val ident = folkeregisteridentifikator["identifikasjonsnummer"].toString()
        val identtype = folkeregisteridentifikator["type"].toString()
        val status = folkeregisteridentifikator["status"].toString()

        if (status != "opphoert") return
        val identer: IdenterResultat = pdlClient.hentIdenter(ident, UUID.randomUUID().toString())
        if (identer !is Identer) return sikkerlogg.info("Kan ikke registrere folkeregisteridentifikator-endring på $ident pga manglende fnr")
        val packet = JsonMessage.newMessage("ident_opphørt", mapOf(
            "fødselsnummer" to ident,
            "identtype" to identtype,
            "aktørId" to identer.aktørId,
            "nye_identer" to mapOf(
                "fødselsnummer" to identer.fødselsnummer,
                "aktørId" to identer.aktørId
            ),
            "lesahHendelseId" to "${record.get("hendelseId")}"
        ))
        sikkerlogg.info("publiserer ident_opphørt for {} {}:\n${packet.toJson()}",
            keyValue("fødselsnummer", identer.fødselsnummer),
            keyValue("aktørId", identer.aktørId)
        )
        rapidsConnection.publish(ident, packet.toJson())
    }

    private fun håndterAdressebeskyttelse(record: GenericRecord) {
        sikkerlogg.info("mottok endring på adressebeskyttelse")

        val ident = (record.get("personidenter") as List<Any?>).first().toString()
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
