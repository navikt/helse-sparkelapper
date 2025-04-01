package no.nav.helse.sparkel.tilbakedatert

import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID
import org.intellij.lang.annotations.Language

internal class TilbakedatertRiverTest {

    private val rapid: TestRapid = TestRapid().apply(::TilbakedatertRiver)

    private fun sendEvent(behov: String) = rapid.sendTestMessage(behov)

    private fun merknaderMedInnhold(type: String) = """ "merknader": [{"type": "$type"}] """
    private fun merknaderErNull() = """ "merknader": null """

    @Language("JSON")
    private fun sykmeldingEvent(fom: LocalDate? = LocalDate.now().minusDays(4), signaturDato: LocalDateTime? = LocalDateTime.now(), merknader: String?) =
        """
        {
             "sykmelding": {
                "id": "${UUID.randomUUID()}",
                "syketilfelleStartDato": "$fom",
                "signaturDato": "$signaturDato",
                "perioder": [
                    {
                        "fom": "$fom",
                        "tom": "${fom?.plusDays(30)}"
                    }
                ]
            },
            "personNrPasient": "12345678910" ${if (merknader != null) "${','}" else ""}
            ${merknader ?: ""}
        }
        """

    @Test
    fun `Sender tilbakedatering_behandlet dersom sykmelding er tilbakedatert og godkjent`() {
        sendEvent(sykmeldingEvent(merknader = merknaderErNull()))

        val svar = rapid.inspektør.message(0)
        assertEquals("tilbakedatering_behandlet", svar["@event_name"].asText())
        assertEquals(LocalDate.now().minusDays(4), svar["syketilfelleStartDato"].asLocalDate())
        assertEquals("12345678910", svar["fødselsnummer"].asText())
    }

    @Test
    fun `Sender tilbakedatering_behandlet dersom sykmelding er tilbakedatert og merknader mangler i meldingen`() {
        sendEvent(sykmeldingEvent(merknader = null))
        assertEquals("tilbakedatering_behandlet", rapid.inspektør.field(0, "@event_name").asText())
    }

    @Test
    fun `Sender tilbakedatering_behandlet dersom sykmelding er tilbakedatert og merknader er tom liste`() {
        sendEvent(sykmeldingEvent(merknader = """ "merknader": [] """))
        assertEquals("tilbakedatering_behandlet", rapid.inspektør.field(0, "@event_name").asText())
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet dersom sykmelding ikke er tilbakedatert`() {
        sendEvent(sykmeldingEvent(fom = LocalDate.now(), signaturDato = LocalDateTime.now(), merknader = merknaderErNull()))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet dersom sykmelding er tilbakedatert men er UNDER_BEHANDLING`() {
        sendEvent(sykmeldingEvent(merknader = merknaderMedInnhold("UNDER_BEHANDLING")))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet dersom UGYLDIG_TILBAKEDATERING`() {
        sendEvent(sykmeldingEvent(merknader = merknaderMedInnhold("UGYLDIG_TILBAKEDATERING")))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet dersom TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER`() {
        sendEvent(sykmeldingEvent(merknader = merknaderMedInnhold("TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER")))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet dersom DELVIS_GODKJENT`() {
        sendEvent(sykmeldingEvent(merknader = merknaderMedInnhold("DELVIS_GODKJENT")))
        assertEquals(0, rapid.inspektør.size)
    }
}
