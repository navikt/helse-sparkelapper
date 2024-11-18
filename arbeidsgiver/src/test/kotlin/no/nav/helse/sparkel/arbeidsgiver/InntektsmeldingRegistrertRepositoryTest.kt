package no.nav.helse.sparkel.arbeidsgiver

import com.github.navikt.tbd_libs.test_support.TestDataSource
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.db.InntektsmeldingRegistrertTable
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertDto
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.jetbrains.exposed.sql.Database as ExposedDatabase

internal class InntektsmeldingRegistrertRepositoryTest {
    private lateinit var testDataSource: TestDataSource
    private lateinit var db: org.jetbrains.exposed.sql.Database
    private lateinit var inntektsmeldingRegistrertRepository: InntektsmeldingRegistrertRepository

    @BeforeEach
    fun setUp() {
        testDataSource = databaseContainer.nyTilkobling()
        db = ExposedDatabase.connect(testDataSource.ds)
        inntektsmeldingRegistrertRepository = InntektsmeldingRegistrertRepository(db)
    }

    @BeforeEach
    fun resetDatabase() {
        databaseContainer.droppTilkobling(testDataSource)
    }

    @Test
    fun `lagrer InntektsmeldingRegistrert i databasen`() {
        transaction(db) {
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

        transaction(db) {
            val actual = InntektsmeldingRegistrertTable.selectAll().single()
            assertEquals(dokumentId, actual[InntektsmeldingRegistrertTable.dokumentId])
            assertEquals(hendelseId, actual[InntektsmeldingRegistrertTable.hendelseId])
            assertEquals(opprettet, actual[InntektsmeldingRegistrertTable.opprettet])
        }
    }

    @Test
    fun `håndterer at vi får samme inntektsmelding flere ganger`() {

        val inntektsmeldingRegistrertDto = InntektsmeldingRegistrertDto(
            hendelseId = UUID.randomUUID(),
            dokumentId = UUID.randomUUID(),
            opprettet = LocalDate.EPOCH.atStartOfDay()
        )

        inntektsmeldingRegistrertRepository.lagre(inntektsmeldingRegistrertDto)
        inntektsmeldingRegistrertRepository.lagre(inntektsmeldingRegistrertDto)
    }

    @Test
    fun `finner dokumentId knyttet til en hendelsesId`() {
        val hendelseId = UUID.randomUUID()
        val expectedDokumentId = UUID.randomUUID()
        val opprettet = LocalDate.EPOCH.atStartOfDay()
        val inntektsmeldingRegistrertDto = InntektsmeldingRegistrertDto(
            hendelseId = hendelseId,
            dokumentId = expectedDokumentId,
            opprettet = opprettet
        )

        inntektsmeldingRegistrertRepository.lagre(inntektsmeldingRegistrertDto)
        transaction {
            assertEquals(1, InntektsmeldingRegistrertTable.selectAll().map { it }.size)
        }

        val actualDokumentId = inntektsmeldingRegistrertRepository.finnDokumentId(hendelseId)
        assertEquals(expectedDokumentId, actualDokumentId)
    }

    @Test
    fun `returnerer null dersom det ikke finnes en knytning mellom dokumentId og hendelsesId`() {
        assertNull(inntektsmeldingRegistrertRepository.finnDokumentId(UUID.randomUUID()))
    }
}