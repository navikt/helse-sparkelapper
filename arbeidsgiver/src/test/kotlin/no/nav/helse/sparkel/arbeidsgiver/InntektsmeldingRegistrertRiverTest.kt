package no.nav.helse.sparkel.arbeidsgiver

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRepository
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRiver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InntektsmeldingRegistrertRiverTest {

    private val testRapid = TestRapid()
    private val repository = mockk<InntektsmeldingRegistrertRepository>()

    init {
        InntektsmeldingRegistrertRiver(testRapid, repository)
    }

    @BeforeEach
    fun clear() {
        clearMocks(repository)
    }

    @Test
    fun `lagrer korrekte meldinger i databasen`() {
        every { repository.lagre(any()) } just runs

        val hendelseId = UUID.randomUUID()
        val dokumentId = UUID.randomUUID()
        val opprettet = LocalDateTime.now()

        testRapid.sendJson(
            hendelseId = hendelseId,
            dokumentId = dokumentId,
            opprettet = opprettet
        )

        verify {
            repository.lagre(match {
                it.hendelseId == hendelseId &&
                it.dokumentId == dokumentId &&
                it.opprettet == opprettet
            })
        }
    }

    @Test
    fun `parser ikke event med feil event_name`() {

        testRapid.sendJson(eventName = "søknad")
        verify(exactly = 0) {
            repository.lagre(any())
        }
    }

    fun TestRapid.sendJson(
        eventName: String = "inntektsmelding",
        hendelseId: UUID = UUID.randomUUID(),
        dokumentId: UUID = UUID.randomUUID(),
        opprettet: LocalDateTime = LocalDateTime.now()
    ) = sendTestMessage(
        """
       {
            "@event_name": "${eventName}",
            "@id": "${hendelseId}",
            "inntektsmeldingId": "${dokumentId}",
            "@opprettet": "${opprettet}"
       } 
    """
    )
}