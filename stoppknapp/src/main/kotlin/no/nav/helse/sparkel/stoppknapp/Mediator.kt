package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.stoppknapp.db.Dao

internal class Mediator(
    rapidsConnection: RapidsConnection,
    private val dao: Dao,
) {
    init {
        StoppknappRiver(rapidsConnection, this)
    }
}
