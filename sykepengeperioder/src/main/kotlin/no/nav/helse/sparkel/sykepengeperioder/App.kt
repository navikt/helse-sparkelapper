package no.nav.helse.sparkel.sykepengeperioder

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.sykepengeperioder.infotrygd.AzureClient
import no.nav.helse.sparkel.sykepengeperioder.infotrygd.InfotrygdClient
import org.flywaydb.core.Flyway
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.Server
import javax.sql.DataSource

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val dataSource: DataSource = JdbcDataSource().apply {
        setUrl("jdbc:h2:mem:test1;MODE=Oracle;DB_CLOSE_DELAY=-1")
        user = "sa"
        password = "sa"
        Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9090").start()

        flyway()
    }
    val azureClient = AzureClient(
        tokenEndpoint = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
        clientId = env.getValue("AZURE_APP_CLIENT_ID"),
        clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET")
    )
    val infotrygdClient = InfotrygdClient(
        baseUrl = env.getValue("INFOTRYGD_URL"),
        accesstokenScope = env.getValue("INFOTRYGD_SCOPE"),
        azureClient = azureClient
    )
    val infotrygdService = InfotrygdService(infotrygdClient, dataSource)

    return RapidApplication.create(env).apply {
        Sykepengehistorikkløser(this, infotrygdService)
        Utbetalingsperiodeløser(this, infotrygdService)
    }
}

private fun DataSource.flyway() {
    Flyway.configure()
        .dataSource(this)
        .load()
        .migrate()
}

