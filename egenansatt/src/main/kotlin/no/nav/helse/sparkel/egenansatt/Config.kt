package no.nav.helse.sparkel.egenansatt

import java.net.URI
import java.net.URL


fun setUpEnvironment(env: Map<String, String> = System.getenv()) =
    Environment(
        raw = env,
        skjermedeBaseURL = URI(env.getValue("SKJERMEDE_BASE_URL")).toURL(),
        skjermendeOauthScope = env.getValue("SKJERMEDE_OAUTH_SCOPE"),
        tokenEndpointURL = URI(env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
        clientId = env.getValue("AZURE_APP_CLIENT_ID"),
        clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET")
    )

data class Environment(
    val raw: Map<String, String>,
    val skjermedeBaseURL: URL,
    val skjermendeOauthScope: String,
    val tokenEndpointURL: URI,
    val clientId: String,
    val clientSecret: String
)
