package no.nav.helse.sparkel.stoppknapp.db

import javax.sql.DataSource

internal class Dao(dataSource: DataSource) : AbstractDao(dataSource) {
    internal fun lagre(stoppknappMelding: StoppknappMeldingTilDatabase) {
        val årsakerForDatabase = stoppknappMelding.årsaker.joinToString { """ $it """ }
        query(
            """
            insert into stoppknapp_melding(fødselsnummer, status, årsaker, tidsstempel, original_melding)
            values (:fodselsnummer, :status, '{$årsakerForDatabase}', :tidsstempel, CAST(:originalMelding as json))
            """.trimIndent(),
            "fodselsnummer" to stoppknappMelding.fødselsnummer,
            "status" to stoppknappMelding.status,
            "tidsstempel" to stoppknappMelding.tidsstempel,
            "originalMelding" to stoppknappMelding.originalMelding,
        ).update()
    }

    internal fun hent(fødselsnummer: String) =
        query(
            "select * from stoppknapp_melding where fødselsnummer = :fodselsnummer",
            "fodselsnummer" to fødselsnummer,
        ).list {
            StoppknappMeldingFraDatabase(
                fødselsnummer = it.string("fødselsnummer"),
                status = it.string("status"),
                årsaker = it.array<String>("årsaker").toList(),
                tidsstempel = it.localDateTime("tidsstempel"),
            )
        }
}
