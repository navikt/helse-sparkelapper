package no.nav.helse.sparkel.sykepengeperioder

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.sykepengeperioder.infotrygd.AzureClient
import no.nav.helse.sparkel.sykepengeperioder.infotrygd.InfotrygdClient
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val dataSource: DataSource? = try {
//        val hikariConfig = HikariConfig().apply {
//            jdbcUrl = env.getValue("INFOTRYGDSP_URL")
//            username = env.getValue("INFOTRYGDSP_USERNAME")
//            password = env.getValue("INFOTRYGDSP_PASSWORD")
//            schema = env.getValue("INFOTRYGDSP_SCHEMA")
//        }
//        HikariDataSource(hikariConfig)
        null
    } catch (e: Throwable) {
        sikkerlogg.warn("Feil ved opprettelse av dataSource", e)
        null
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
