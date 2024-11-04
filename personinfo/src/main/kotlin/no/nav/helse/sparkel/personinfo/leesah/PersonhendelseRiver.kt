package no.nav.helse.sparkel.personinfo.leesah

import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.retry.retryBlocking
import com.github.navikt.tbd_libs.speed.IdentResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.withMDC
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.slf4j.LoggerFactory

internal fun erDev() = "dev-gcp" == System.getenv("NAIS_CLUSTER_NAME")

internal class PersonhendelseRiver(
    private val rapidsConnection: RapidsConnection,
    private val speedClient: SpeedClient,
    private val cacheTimeout: Duration
) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private var forrigeIdent: String? = null
    private var forrigeOppdatering = LocalDateTime.MIN

    fun onPackage(record: GenericRecord) {
        val opplysningstype = record.get("opplysningstype").toString()
        val callId = UUID.randomUUID().toString()
        withMDC("callId" to callId) {
            when (opplysningstype) {
                "ADRESSEBESKYTTELSE_V1" -> håndterAdressebeskyttelse(record, callId)
                "DOEDSFALL_V1" -> håndterDødsmelding(record, callId)
                "FOLKEREGISTERIDENTIFIKATOR_V1" -> håndterFolkeregisteridentifikator(record, callId)
                else -> sikkerlogg.info("uhåndtert melding {}\n$record", keyValue("opplysningstype", opplysningstype))
            }
        }
    }

    private fun håndterDødsmelding(record: GenericRecord, callId: String) {
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

        val identer = try {
            retryBlocking {
                when (val svar = speedClient.hentFødselsnummerOgAktørId(ident, callId)) {
                    is Result.Error -> throw RuntimeException(svar.error, svar.cause)
                    is Result.Ok -> svar.value
                }
            }
        } catch (err: Exception) {
            return sikkerlogg.info("Kan ikke registrere dødsdato-endring på $ident pga manglende fnr", err)
        }

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

    private fun håndterFolkeregisteridentifikator(record: GenericRecord, callId: String) {
        sikkerlogg.info("mottok melding om folkeregisteridentifikator:\n$record")

        val folkeregisteridentifikator = record.get("Folkeregisteridentifikator")
        if (folkeregisteridentifikator !is GenericData.Record) return
        val ident = folkeregisteridentifikator["identifikasjonsnummer"].toString()
        val identtype = folkeregisteridentifikator["type"].toString()
        val status = folkeregisteridentifikator["status"].toString()
        if (status != "opphoert") return

        val historiskeIdenter = try {
            retryBlocking {
                when (val svar = speedClient.hentHistoriskeFødselsnumre(ident, callId)) {
                    is Result.Error -> throw RuntimeException(svar.error, svar.cause)
                    is Result.Ok -> svar.value
                }
            }
        } catch (err: Exception) {
            if (erDev()) {
                sikkerlogg.error("Fikk feil ved pdl-oppslag på ident $ident, ignorerer melding", err)
                return
            }
            throw err
        }

        val aktivIdent = try {
            retryBlocking {
                when (val svar = speedClient.hentFødselsnummerOgAktørId(ident, callId)) {
                    is Result.Error -> throw RuntimeException(svar.error, svar.cause)
                    is Result.Ok -> svar.value
                }
            }
        } catch (err: Exception) {
            if (erDev()) {
                sikkerlogg.error("Fikk feil ved pdl-oppslag på ident $ident, ignorerer melding", err)
                return
            }
            throw err
        }

        val packet = JsonMessage.newMessage("ident_opphørt", mapOf(
            "fødselsnummer" to ident,
            "identtype" to identtype,
            "aktørId" to aktivIdent.aktørId,
            "nye_identer" to mapOf(
                "fødselsnummer" to aktivIdent.fødselsnummer,
                "aktørId" to aktivIdent.aktørId,
                "npid" to aktivIdent.npid
            ),
            "gamle_identer" to historiskeIdenter.fødselsnumre.map {
                mapOf(
                    "type" to "FØDSELSNUMMER",
                    "ident" to it
                )
            },
            "lesahHendelseId" to "${record.get("hendelseId")}"
        ))
        sikkerlogg.info("publiserer ident_opphørt for {} {}:\n${packet.toJson()}",
            keyValue("fødselsnummer", aktivIdent.fødselsnummer),
            keyValue("aktørId", aktivIdent.aktørId)
        )
        rapidsConnection.publish(ident, packet.toJson())
    }

    private fun håndterAdressebeskyttelse(record: GenericRecord, callId: String) {
        sikkerlogg.info("mottok endring på adressebeskyttelse")

        val ident = (record.get("personidenter") as List<Any?>).first().toString()
        if (throttle(ident)) return

        val aktivIdent = try {
            retryBlocking {
                when (val svar = speedClient.hentFødselsnummerOgAktørId(ident, callId)) {
                    is Result.Error -> throw RuntimeException(svar.error, svar.cause)
                    is Result.Ok -> svar.value
                }
            }
        } catch (err: Exception) {
            return sikkerlogg.error("Fikk feil ved pdl-oppslag på ident $ident, ignorerer melding", err)
        }
        sendMelding(aktivIdent)
    }

    private fun sendMelding(identer: IdentResponse) {
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
