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

        internal fun Map<String, String>.databaseConfig() = DatabaseConfig(
            username = File("/var/run/secrets/nais.io/oracle/creds/username").readText(),
            password = File("/var/run/secrets/nais.io/oracle/creds/password").readText(),
            jdbcUrl = File("/var/run/secrets/nais.io/oracle/config/jdbc_url").readText(),
            schema = getValue("DATABASE_SCHEMA")
        )

        internal fun Map<String, String>.databaseConfigForFeriepenger(): DatabaseConfig {
            logger.info("Kobler opp mot felles replika til bruk for beregning av feriepenger.")
            return DatabaseConfig(
                username = getValue("INFOTRYGDSP_USERNAME"),
                password = getValue("INFOTRYGDSP_PASSWORD"),
                jdbcUrl = getValue("INFOTRYGDSP_URL"),
                schema = getValue("INFOTRYGDSP_SCHEMA")
            )
        }
    }
}