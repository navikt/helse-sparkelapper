package no.nav.helse.sparkel.vilkarsproving

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val vaultServiceUserBase = "/var/run/secrets/nais.io/service_user"
val vaultServiceUserBasePath: Path = Paths.get(vaultServiceUserBase)

fun readServiceUserCredentials() = ServiceUser(
    username = Files.readString(vaultServiceUserBasePath.resolve("username")),
    password = Files.readString(vaultServiceUserBasePath.resolve("password"))
)

fun setUpEnvironment() =
    Environment(
        raw = System.getenv(),
        stsSoapBaseUrl = System.getenv("STS_URL"),
        aaregBaseUrl = System.getenv("AAREG_BASE_URL")
            ?: error("Mangler env var FPSAK_BASE_URL"),
        egenAnsattBaseUrl = System.getenv("EGENANSATT_URL")
            ?: error("Mangler env var EGENANSATT_URL"),
        nomBaseURL = System.getenv("NOM_BASE_URL")?.let {URL(it)}
            ?: error("Mangler env var NOM_BASE_URL"),
        nomOauthScope = System.getenv("NOM_OAUTH_SCOPE")
            ?: error("Mangler env var NOM_OAUTH_SCOPE"),
        tokenEndpointURL = System.getenv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")?.let {URL(it)}
            ?: error("Mangler env var AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
        clientId = System.getenv("AZURE_APP_CLIENT_ID")
            ?: error("Mangler env var AZURE_APP_CLIENT_ID"),
        clientSecret = System.getenv("AZURE_APP_CLIENT_SECRET")
            ?: error("Mangler env var AZURE_APP_CLIENT_SECRET")
    )

data class Environment(
    val raw: Map<String, String>,
    val stsBaseUrl: String = "http://security-token-service.default.svc.nais.local",
    val stsSoapBaseUrl: String,
    val aaregBaseUrl: String,
    val egenAnsattBaseUrl: String,
    val nomBaseURL: URL,
    val nomOauthScope: String,
    val tokenEndpointURL: URL,
    val clientId: String,
    val clientSecret: String
)

data class ServiceUser(
    val username: String,
    val password: String
) {
}
