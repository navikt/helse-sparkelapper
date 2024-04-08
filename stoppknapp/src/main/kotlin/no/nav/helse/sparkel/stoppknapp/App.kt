package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.stoppknapp.db.Dao
import no.nav.helse.sparkel.stoppknapp.db.DataSourceBuilder

internal class App : RapidsConnection.StatusListener {
    private val env = System.getenv()
    private val rapidsConnection = RapidApplication.create(env)
    private val datasourceBuilder = DataSourceBuilder(env)
    private val dataSource = datasourceBuilder.getDataSource()

    private val dao = Dao(dataSource)

    init {
        rapidsConnection.register(this)
        Mediator(rapidsConnection, dao)
    }

    internal fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        datasourceBuilder.migrate()
    }
}

internal fun main() {
    App().start()
}
