package no.nav.helse.sparkel.identer.db

import java.time.LocalDateTime
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.identer.AktørV2
import no.nav.helse.sparkel.identer.Identifikator
import no.nav.helse.sparkel.identer.Type
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class IdentifikatorDaoTest : AbstractDatabaseTest() {

    private val timestampFørTestkjøring = LocalDateTime.now()

    private lateinit var identifikatorDao: IdentifikatorDao

    private val identifikatorer = listOf(
        Identifikator("111", Type.FOLKEREGISTERIDENT, true),
        Identifikator("222", Type.FOLKEREGISTERIDENT, false),
        Identifikator("111222", Type.AKTORID, true)
    )

    @BeforeAll
    fun setup() {
        identifikatorDao = IdentifikatorDao(dataSource)
    }

    @Test
    @Order(1)
    fun `lagre aktør`() {
        val aktørV2 = AktørV2(identifikatorer = identifikatorer, key = "000")
        identifikatorDao.lagreAktør(aktørV2)
        assertEquals(3L, count())
    }

    @Test
    @Order(2)
    fun `lagre aktør med tidligere lagret key skal kaste exception`() {
        val aktørV2 = AktørV2(
            identifikatorer = listOf(Identifikator("555", Type.FOLKEREGISTERIDENT, true)),
            key = "000"
        )
        assertThrows<RuntimeException> {
            identifikatorDao.lagreAktør(aktørV2)
        }
    }

    @Test
    @Order(3)
    fun `hent aktør med fnr`() {
        val lagretAktør = identifikatorDao.hentIdenterForFødselsnummer("222")
        assertNotNull(lagretAktør)
        assertEquals("000", lagretAktør!!.key)
        assertEquals(3, lagretAktør.identifikatorer.size)
        assertTrue(lagretAktør.identifikatorer.containsAll(identifikatorer))
    }

    @Test
    @Order(4)
    fun `hent aktør med aktørid`() {
        val lagretAktør = identifikatorDao.hentIdenterForAktørid("111222")
        assertNotNull(lagretAktør)
        assertEquals("000", lagretAktør!!.key)
        assertEquals(3, lagretAktør.identifikatorer.size)
        assertTrue(lagretAktør.identifikatorer.containsAll(identifikatorer))
    }

    @Test
    @Order(5)
    fun `oppslag på ikke-eksisterende identifikator`() {
        assertNull(identifikatorDao.hentIdenterForFødselsnummer("777"))
    }

    @Test
    @Order(6)
    fun `lagring for eksisterende ident skal erstatte alle tidligere identer for tilhørende person`() {
        // Gjør en sjekk på at noe allerede er lagret i tabellen.
        // Dette vil være data på samme person som nye data skal lagres for, testen vil feile i senere steg om dette
        // ikke er tilfelle.
        assertEquals(3L, count())
        val nyIdentifikator = Identifikator("333", Type.FOLKEREGISTERIDENT, true)


        val nyeIdentifikatorer = identifikatorer.map {
            if (it.type == Type.FOLKEREGISTERIDENT) {
                // Setter alle fnr/dnr som ikke gjeldende slik at det kun er en ident, den nye, som vil være den gjeldende.
                // Mest for å gi testcaset et logisk innhold, kunne vært droppet.
                it.copy(gjeldende = false)
            } else it
        }.plus(nyIdentifikator)

        val aktørV2 = AktørV2(identifikatorer = nyeIdentifikatorer, key = "001")
        identifikatorDao.lagreAktør(aktørV2)
        assertEquals(nyeIdentifikatorer.size.toLong(), count())

        val lagretAktør = identifikatorDao.hentIdenterForFødselsnummer("333")
        assertEquals("001", lagretAktør!!.key)
        assertEquals(4, lagretAktør.identifikatorer.size)
        assertTrue(lagretAktør.identifikatorer.containsAll(nyeIdentifikatorer))
    }

    @Test
    @Order(7)
    fun `sjekk at kolonnen melding_lest er blitt populert`() {
        // Denne kolonnen er i utgangspunktet ikke eksponert i appen, men tiltenkt som kjekk å ha ved oppslag
        // direkte mot databasen ved f.eks feilsøk og uthenting av statistikk.
        val meldingLestVerdier = meldingLestVerdier()
        assertTrue(meldingLestVerdier.size > 0)
        assertTrue(meldingLestVerdier.all { it.isAfter(timestampFørTestkjøring)})
    }

    private fun count() = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val query = "SELECT count(1) as antall FROM identifikator"
        session.run(
            queryOf(query).map { it.long("antall") }.asSingle
        )
    }

    private fun meldingLestVerdier() = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val query = "SELECT melding_lest FROM identifikator"
        session.run(
            queryOf(query).map { it.localDateTime("melding_lest") }.asList
        )
    }
}