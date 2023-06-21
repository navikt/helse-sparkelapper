package no.nav.helse.sparkel.arbeidsgiver

import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.db.Database
import no.nav.helse.sparkel.arbeidsgiver.db.InntektsmeldingRegistrertTable
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertDto
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRepository
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.jetbrains.exposed.sql.Database as ExposedDatabase

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class InntektsmeldingRegistrertRepositoryTest {
    private val database = Database(dbConfig())
        .configureFlyway()

    private val inntektsmeldingRegistrertRepository = InntektsmeldingRegistrertRepository()

    @BeforeAll
    fun setUp() {
        ExposedDatabase.connect(database.dataSource)
    }

    @BeforeEach
    fun resetDatabase() {
        transaction {
            InntektsmeldingRegistrertTable.deleteAll()
        }
    }

    @Test
    fun `lagrer InntektsmeldingRegistrert i databasen`() {
        transaction {
            assertEquals(0, InntektsmeldingRegistrertTable.selectAll().map { it }.size)
        }

        val hendelseId = UUID.randomUUID()
        val dokumentId = UUID.randomUUID()
        val opprettet = LocalDate.EPOCH.atStartOfDay()
        val inntektsmeldingRegistrertDto = InntektsmeldingRegistrertDto(
            hendelseId = hendelseId,
            dokumentId = dokumentId,
            opprettet = opprettet
        )

        inntektsmeldingRegistrertRepository.lagre(inntektsmeldingRegistrertDto)

        transaction {
            val actual = InntektsmeldingRegistrertTable.selectAll().single()
            assertEquals(dokumentId, actual[InntektsmeldingRegistrertTable.dokumentId])
            assertEquals(hendelseId, actual[InntektsmeldingRegistrertTable.hendelseId])
            assertEquals(opprettet, actual[InntektsmeldingRegistrertTable.opprettet])
        }

    }

    @Test
    fun `lagrer ikke InntektsmeldingRegistrert med lik dokumentId og hendelseId i databasen`() {

        val inntektsmeldingRegistrertDto = InntektsmeldingRegistrertDto(
            hendelseId = UUID.randomUUID(),
            dokumentId = UUID.randomUUID(),
            opprettet = LocalDate.EPOCH.atStartOfDay()
        )

        assertThrows<ExposedSQLException> {
            inntektsmeldingRegistrertRepository.lagre(inntektsmeldingRegistrertDto)
            inntektsmeldingRegistrertRepository.lagre(inntektsmeldingRegistrertDto)
        }
    }
}