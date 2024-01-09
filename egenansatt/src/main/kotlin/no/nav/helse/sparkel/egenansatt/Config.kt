package no.nav.helse.sparkel.egenansatt

import java.net.URL


fun setUpEnvironment() =
    Environment(
        raw = System.getenv(),
        skjermedeBaseURL = System.getenv("SKJERMEDE_BASE_URL")?.let {URL(it)}
            ?: error("Mangler env var SKJERMEDE_BASE_URL"),
        skjermendeOauthScope = System.getenv("SKJERMEDE_OAUTH_SCOPE")
            ?: error("Mangler env var SKJERMEDE_OAUTH_SCOPE"),
        tokenEndpointURL = System.getenv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")?.let {URL(it)}
            ?: error("Mangler env var AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
        clientId = System.getenv("AZURE_APP_CLIENT_ID")
            ?: error("Mangler env var AZURE_APP_CLIENT_ID"),
        clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET")
            ?: error("Mangler env var AZURE_APP_CLIENT_SECRET")
    )

data class Environment(
    val raw: Map<String, String>,
    val skjermedeBaseURL: URL,
    val skjermendeOauthScope: String,
    val tokenEndpointURL: URL,
    val clientId: String,
    val clientSecret: String
)
