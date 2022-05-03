package no.nav.helse.sparkel.aareg.util

import java.net.URL

fun setUpEnvironment() =
    Environment(
        raw = System.getenv(),
        organisasjonBaseUrl = System.getenv("ORGANISASJON_BASE_URL")
            ?: error("Mangler env var ORGANISASJON_BASE_URL"),
        kodeverkBaseUrl = System.getenv("KODEVERK_BASE_URL")
            ?: error("Mangler env var KODEVERK_BASE_URL"),
        aaregBaseUrlRest = System.getenv("AAREG_BASE_URL_REST")
            ?: error("Mangler env var AAREG_BASE_URL_REST"),
        aaregOauthScope = System.getenv("AAREG_OAUTH_SCOPE")
            ?: error("Mangler env var AAREG_OAUTH_SCOPE"),
        tokenEndpointURL = System.getenv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")?.let { URL(it) }
            ?: error("Mangler env var AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
        clientId = System.getenv("AZURE_APP_CLIENT_ID")
            ?: error("Mangler env var AZURE_APP_CLIENT_ID"),
        clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET")
            ?: error("Mangler env var AZURE_APP_CLIENT_SECRET"),
        appName = System.getenv("NAIS_APP_NAME") ?: "sparkel-aareg"
    )

data class Environment(
    val raw: Map<String, String>,
    val organisasjonBaseUrl: String,
    val kodeverkBaseUrl: String,
    val aaregBaseUrlRest: String,
    val aaregOauthScope: String,
    val tokenEndpointURL: URL,
    val clientId: String,
    val clientSecret: String,
    val appName: String
)
