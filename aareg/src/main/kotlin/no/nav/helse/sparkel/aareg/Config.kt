package no.nav.helse.sparkel.aareg

import java.net.URI

fun setUpEnvironment() =
    Environment(
        raw = System.getenv(),
        organisasjonBaseUrl = fromEnv("ORGANISASJON_BASE_URL"),
        kodeverkBaseUrl = fromEnv("KODEVERK_BASE_URL"),
        kodeverkOauthScope = fromEnv("KODEVERK_OAUTH_SCOPE"),
        aaregBaseUrlRest = fromEnv("AAREG_BASE_URL_REST"),
        aaregOauthScope = fromEnv("AAREG_OAUTH_SCOPE"),
        tokenEndpointURL = URI(fromEnv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
        clientId = fromEnv("AZURE_APP_CLIENT_ID"),
        clientSecret = fromEnv("AZURE_APP_CLIENT_SECRET"),
        appName = System.getenv("NAIS_APP_NAME") ?: "sparkel-aareg"
    )

private fun fromEnv(name: String) = System.getenv(name) ?: error("Mangler env var $name")

data class Environment(
    val raw: Map<String, String>,
    val organisasjonBaseUrl: String,
    val kodeverkBaseUrl: String,
    val kodeverkOauthScope: String,
    val aaregBaseUrlRest: String,
    val aaregOauthScope: String,
    val tokenEndpointURL: URI,
    val clientId: String,
    val clientSecret: String,
    val appName: String
)
