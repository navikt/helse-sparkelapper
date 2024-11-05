package no.nav.helse.sparkel.skjermetendret

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import java.time.LocalDateTime
import java.util.UUID
import org.slf4j.LoggerFactory

class SkjermetEndretPubliserer(
    private val rapidConnection: RapidsConnection
) {
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    fun publiserSkjermetEndring(fnr: String, skjermet: Boolean) {
        sikkerlogg.info("Publiserer endring på skjermet-status for fnr: $fnr")

        val packet: JsonMessage = JsonMessage.newMessage(
            mapOf(
                "@event_name" to "endret_skjermetinfo",
                "@id" to UUID.randomUUID(),
                "@opprettet" to LocalDateTime.now(),
                "fødselsnummer" to fnr,
                "skjermet" to skjermet
            )
        )
        rapidConnection.publish(fnr, packet.toJson())
    }
}