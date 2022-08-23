package no.nav.helse.sparkel.sykepengeperioder

import java.io.File
import org.slf4j.LoggerFactory

internal class DatabaseConfig private constructor(
    val username: String,
    val password: String,
    val jdbcUrl: String,
    val schema: String
) {
    internal companion object {
        private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)

        internal fun Map<String, String>.databaseConfig(): DatabaseConfig {
            val usernameFile = File("/var/run/secrets/nais.io/oracle/creds/username")
            val passwordFile = File("/var/run/secrets/nais.io/oracle/creds/password")
            val jdbcUrlFile = File("/var/run/secrets/nais.io/oracle/config/jdbc_url")
            val schema = get("DATABASE_SCHEMA")
            return if (usernameFile.exists() && passwordFile.exists() && jdbcUrlFile.exists() && schema != null) {
                logger.info("Kobler opp mot sykepengers egen replika.")
                DatabaseConfig(
                    username = usernameFile.readText(),
                    password = passwordFile.readText(),
                    jdbcUrl = jdbcUrlFile.readText(),
                    schema = schema
                )
            } else {
                logger.info("Kobler opp mot felles replika.")
                DatabaseConfig(
                    username = getValue("INFOTRYGDSP_USERNAME"),
                    password = getValue("INFOTRYGDSP_PASSWORD"),
                    jdbcUrl = getValue("INFOTRYGDSP_URL"),
                    schema = getValue("INFOTRYGDSP_SCHEMA")
                )
            }
        }
    }
}