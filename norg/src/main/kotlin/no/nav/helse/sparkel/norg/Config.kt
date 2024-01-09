package no.nav.helse.sparkel.norg

import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64

private val serviceuserBasePath = Paths.get("/var/run/secrets/nais.io/service_user")

fun readServiceUserCredentials() = ServiceUser(
    username = Files.readString(serviceuserBasePath.resolve("username")),
    password = Files.readString(serviceuserBasePath.resolve("password"))
)

class ServiceUser(
    val username: String,
    val password: String
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}

fun readEnvironment(env: Map<String, String> = System.getenv()) = Environment(
    tokenEndpoint = env["AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"],
    clientId = env["AZURE_APP_CLIENT_ID"],
    clientSecret = env["AZURE_APP_CLIENT_SECRET"],
    norg2BaseUrl = env.getValue("NORG2_BASE_URL"),
    norg2Scope = env["NORG2_SCOPE"],
    pdlUrl = env.getValue("PDL_URL"),
    pdlScope = env["PDL_SCOPE"],
    securityTokenServiceUrl = env["SECURITY_TOKEN_SERVICE_URL"]
)

data class Environment(
    val tokenEndpoint: String?,
    val clientId: String?,
    val clientSecret: String?,
    val norg2BaseUrl: String,
    val norg2Scope: String?,
    val pdlUrl: String,
    val pdlScope: String?,
    val securityTokenServiceUrl: String?
)
