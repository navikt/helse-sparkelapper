package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.stoppknapp.db.Dao
import no.nav.helse.sparkel.stoppknapp.db.StoppknappMeldingTilDatabase
import no.nav.helse.sparkel.stoppknapp.kafka.StoppknappRiver

internal class Mediator(
    rapidsConnection: RapidsConnection,
    private val dao: Dao,
) {
    init {
        StoppknappRiver(rapidsConnection, this)
    }

    internal fun lagre(stoppknappMelding: StoppknappMeldingTilDatabase) {
        dao.lagre(stoppknappMelding)
    }
}
