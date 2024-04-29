package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.stoppknapp.db.Dao
import no.nav.helse.sparkel.stoppknapp.db.StoppknappMeldingFraDatabase
import no.nav.helse.sparkel.stoppknapp.db.StoppknappMeldingTilDatabase
import no.nav.helse.sparkel.stoppknapp.kafka.AutomatiseringStoppetAvVeilederLøser
import no.nav.helse.sparkel.stoppknapp.kafka.Løsning
import no.nav.helse.sparkel.stoppknapp.kafka.StoppknappRiver
import org.slf4j.LoggerFactory

internal class Mediator(
    rapidsConnection: RapidsConnection,
    private val dao: Dao,
) {
    private val logg = LoggerFactory.getLogger(this::class.java)

    init {
        StoppknappRiver(rapidsConnection)
        AutomatiseringStoppetAvVeilederLøser(rapidsConnection, this)
    }

    internal fun lagre(stoppknappMelding: StoppknappMeldingTilDatabase) = dao.lagre(stoppknappMelding)

    internal fun erAutomatiseringStoppet(fødselsnummer: String): Løsning = dao.hent(fødselsnummer).filtrerGjeldendeStopp().tilLøsning()

    private fun List<StoppknappMeldingFraDatabase>.tilLøsning(): Løsning =
        Løsning(
            automatiseringStoppet = this.isNotEmpty(),
            årsaker = this.flatMap { it.årsaker }.toSet(),
        )

    private fun List<StoppknappMeldingFraDatabase>.filtrerGjeldendeStopp(): List<StoppknappMeldingFraDatabase> {
        val gjeldende = mutableListOf<StoppknappMeldingFraDatabase>()
        this.sortedWith { a, b ->
            a.tidsstempel.compareTo(b.tidsstempel)
        }.forEach {
            when (it.status) {
                "STOPP_AUTOMATIKK" -> gjeldende += it
                "NORMAL" -> gjeldende.clear()
                else -> {
                    logg.error("Ukjent status-type: {}", it.status)
                    gjeldende += it
                }
            }
        }
        return gjeldende
    }
}
