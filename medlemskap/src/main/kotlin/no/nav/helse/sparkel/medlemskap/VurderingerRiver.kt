package no.nav.helse.sparkel.medlemskap

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory

internal class VurderingerRiver(rapidsConnection: RapidsConnection) : River.PacketListener {

    // https://github.com/navikt/medlemskap-sykepengerlytter/blob/e4abb7bd5796357e9fe9bc1e6da77506f761c560/src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/speil_medlemskapsvurdering/SpeilRespons.kt#L5-L10
    private val forventedeSpeilSvarVerdier = setOf("JA", "NEI", "UAVKLART", "UAVKLART_MED_BRUKERSPORSMAAL")

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireKey("soknadId", "fnr")
                it.require("speilSvar") { node -> check(node.asString() in forventedeSpeilSvarVerdier) }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        log.info("Mottok medlemskapsvurdering")
        sikkerlogg.debug("Mottok medlemskapsvurdering: ${packet.toJson()}")
    }

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(VurderingerRiver::class.java)
    }
}
