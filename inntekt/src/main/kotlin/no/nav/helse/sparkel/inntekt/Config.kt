package no.nav.helse.sparkel.inntekt

import java.net.URI

fun setUpEnvironment() = Environment(
    aadTokenEndpoint = URI(fromEnv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
    aadClientId = fromEnv("AZURE_APP_CLIENT_ID"),
    aadClientSecret = fromEnv("AZURE_APP_CLIENT_SECRET"),
    inntektRestUrl = fromEnv("INNTEKTSKOMPONENTEN_BASE_URL"),
    inntektOAuthScope = fromEnv("INNTEKTSKOMPONENTEN_OAUTH_SCOPE"),
)

private fun fromEnv(name: String) = System.getenv(name) ?: error("Mangler env var $name")

data class Environment(
    val raw: Map<String, String> = System.getenv(),
    val aadTokenEndpoint: URI,
    val aadClientId: String,
    val aadClientSecret: Any,
    val inntektRestUrl: String,
    val inntektOAuthScope: String,
)
