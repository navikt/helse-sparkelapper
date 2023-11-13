package no.nav.helse.sparkel.sykepengeperioder.dbting

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource
import no.nav.helse.sparkel.infotrygd.Fnr

class StatslønnDAO(
    private val dataSource: DataSource
) {
    internal fun harStatslønn(fnr: Fnr, seq: Int): Boolean {
        return sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val statement = """
                select
                        count(*) harStatslonn
                from is_diverse_11
                where f_nr = ?
                  and is10_arbufoer_seq = ?
                """
            requireNotNull(
                session.run(
                    queryOf(statement, fnr.formatAsITFnr(), seq).map { rs ->
                        rs.int("harStatslonn") > 0
                    }.asSingle
                )
            )
        }
    }
}
