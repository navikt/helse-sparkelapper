package no.nav.helse.sparkel.stoppknapp.db

import kotliquery.Query
import kotliquery.Row
import kotliquery.action.QueryAction
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.stoppknapp.Testdata.FØDSELSNUMMER
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals

internal abstract class DatabaseIntegrationTest : AbstractDatabaseTest() {
    internal val dao = Dao(dataSource)

    protected fun query(
        @Language("postgresql") query: String,
        vararg params: Pair<String, Any?>,
    ) = queryOf(query, params.toMap())

    protected fun <T> Query.single(mapper: (Row) -> T?) = map(mapper).asSingle.runInSession()

    protected fun <T> Query.list(mapper: (Row) -> T?) = map(mapper).asList.runInSession()

    protected fun Query.update() = asUpdate.runInSession()

    protected fun Query.execute() = asExecute.runInSession()

    private fun <T> QueryAction<T>.runInSession() = sessionOf(dataSource).use(::runWithSession)

    protected fun assertLagret(fødselsnummer: String = FØDSELSNUMMER) {
        val fødselsnummerFraDb =
            query(
                "select * from stoppknapp_melding where fødselsnummer = :fodselsnummer",
                "fodselsnummer" to fødselsnummer,
            ).single {
                it.string("fødselsnummer")
            }
        assertEquals(fødselsnummer, fødselsnummerFraDb)
    }
}
