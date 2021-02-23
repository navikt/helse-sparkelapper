package no.nav.helse.sparkel.aareg

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

const val vaultServiceUserBase = "/var/run/secrets/nais.io/service_user"
val vaultServiceUserBasePath: Path = Paths.get(vaultServiceUserBase)

fun readServiceUserCredentials() = ServiceUser(
    username = Files.readString(vaultServiceUserBasePath.resolve("username")),
    password = Files.readString(vaultServiceUserBasePath.resolve("password"))
)

fun setUpEnvironment() =
    Environment(
        raw = System.getenv(),
        stsSoapBaseUrl = System.getenv("STS_BASE_SOAP_URL"),
        organisasjonBaseUrl = System.getenv("ORGANISASJON_BASE_URL")
            ?: error("Mangler env var ORGANISASJON_BASE_URL"),
        kodeverkBaseUrl = System.getenv("KODEVERK_BASE_URL")
            ?: error("Mangler env var KODEVERK_BASE_URL"),
        aaregBaseUrl = System.getenv("AAREG_BASE_URL")
            ?: error("Mangler env var FPSAK_BASE_URL"),
        appName = System.getenv("NAIS_APP_NAME") ?: "sparkel-aareg"
    )

data class Environment(
    val raw: Map<String, String>,
    val stsBaseUrl: String = "http://security-token-service.default.svc.nais.local",
    val stsSoapBaseUrl: String,
    val organisasjonBaseUrl: String,
    val kodeverkBaseUrl: String,
    val aaregBaseUrl: String,
    val appName: String
)

data class ServiceUser(
    val username: String,
    val password: String
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}
