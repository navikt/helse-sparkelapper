package no.nav.helse.sparkel.sykepengeperioder

import java.io.File

internal class DatabaseConfig private constructor(
    val username: String,
    val password: String,
    val jdbcUrl: String,
    val schema: String
) {
    internal companion object {
        internal fun Map<String, String>.databaseConfig() = DatabaseConfig(
            username = File("/var/run/secrets/nais.io/oracle/creds/username").readText(),
            password = File("/var/run/secrets/nais.io/oracle/creds/password").readText(),
            jdbcUrl = File("/var/run/secrets/nais.io/oracle/config/jdbc_url").readText(),
            schema = getValue("DATABASE_SCHEMA")
        )
    }
}
