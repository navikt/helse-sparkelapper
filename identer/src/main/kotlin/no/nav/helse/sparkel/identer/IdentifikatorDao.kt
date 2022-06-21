package no.nav.helse.sparkel.identer

import java.time.LocalDateTime
import javax.sql.DataSource
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language

class IdentifikatorDao(
    private val dataSource: DataSource
) {
    fun lagreAktør(aktorV2: AktørV2) = sessionOf(dataSource).use { session ->

        val idnumre =
            aktorV2.identifikatorer.map { it.idnummer }.joinToString(separator = "','", prefix = "'", postfix = "'")

        @Language("PostgreSQL")
        val queryPersonKeyExists =
            "SELECT 1 FROM identifikator WHERE person_key = ?"
        @Language("PostgreSQL")
        val queryPersonKey =
            "SELECT person_key FROM identifikator WHERE idnummer IN ($idnumre) LIMIT 1"
        @Language("PostgreSQL")
        val deleteSQL =
            "DELETE FROM identifikator WHERE person_key = ?"
        @Language("PostgreSQL")
        val insertSQL =
            "INSERT INTO identifikator (idnummer, type, gjeldende, person_key, melding_lest) values (?, ?, ?, ?, ?)"

        session.transaction { tx ->
            tx.run(
                queryOf(queryPersonKey)
                    .map { it.string("person_key") }
                    .asSingle
            )?.also {
                tx.run(queryOf(deleteSQL, it).asUpdate)
            }

            tx.run(
                queryOf(queryPersonKeyExists, aktorV2.key).map { it.int(1) }.asSingle
            )?.also { throw RuntimeException("Duplikat personKey ${aktorV2.key} funnet, kan ikke persistere innholdet i meldingen") }

            val key = aktorV2.key
            val meldingLest = LocalDateTime.now()
            aktorV2.identifikatorer.forEach { identifikator ->
                tx.run(
                    queryOf(
                        insertSQL,
                        identifikator.idnummer,
                        identifikator.type.name,
                        identifikator.gjeldende,
                        key,
                        meldingLest
                    ).asUpdate
                )
            }
        }
    }

    fun hentIdenterForFødselsnummer(fnr: String): AktørV2? = sessionOf(dataSource).use { session ->
        val personKey = hentKeyForIdent(fnr, Type.FOLKEREGISTERIDENT) ?: return null
        hentIdenter(personKey, session)
    }

    fun hentIdenterForAktørid(fnr: String): AktørV2? = sessionOf(dataSource).use { session ->
        val personKey = hentKeyForIdent(fnr, Type.AKTORID) ?: return null
        hentIdenter(personKey, session)
    }

    private fun hentIdenter(personKey: String, session: Session): AktørV2? {
        @Language("PostgreSQL")
        val query = "SELECT idnummer, type, gjeldende, melding_lest FROM identifikator where person_key = ?"
        val identifikatorer = session.run(
            queryOf(query, personKey).map {
                Identifikator(
                    idnummer = it.string("idnummer"),
                    type = Type.valueOf(it.string("type")),
                    gjeldende = it.boolean("gjeldende")
                )
            }.asList
        )
        return AktørV2(identifikatorer = identifikatorer, key = personKey)
    }

    private fun hentKeyForIdent(ident: String, type: Type) = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val query = "SELECT person_key FROM identifikator where type = ? AND idnummer = ?"
        session.run(
            queryOf(query, type.name, ident).map { it.string("person_key") }.asSingle
        )
    }
}