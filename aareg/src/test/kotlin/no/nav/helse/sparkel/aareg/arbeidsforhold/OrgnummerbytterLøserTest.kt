package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.http.HttpStatusCode
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.orgnummerbytteArbeidsforholdResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OrgnummerbytterLøserTest : AbstractAaregTest() {

    @Test
    fun `Mapper relevante orgnummerbytter fra Aareg`() {
        settOppApp(AaregSvar(orgnummerbytteArbeidsforholdResponse(), HttpStatusCode.OK))

        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["Orgnummerbytter"], "fødselsnummer": "fnr", "vedtaksperiodeId": "id" }"""
        rapid.sendTestMessage(behov)

        val forventetLøsning = listOf(Orgnummerbytte(
            ByttetFra("underenhet1", LocalDate.of(2024, 1, 1)),
            ByttetTil("underenhet2", LocalDate.of(2024, 1, 2))
        ))
        assertEquals(forventetLøsning, sendtMelding.løsning("Orgnummerbytter"))
    }

    private fun JsonNode.løsning(behov: String): List<Orgnummerbytte> =
        this.path("@løsning")
            .path(behov)
            .map {
                Orgnummerbytte(
                    ByttetFra(it["byttetFra"]["orgnummer"].asText(), it["byttetFra"]["sluttDato"].asLocalDate()),
                    ByttetTil(it["byttetTil"]["orgnummer"].asText(), it["byttetTil"]["startDato"].asLocalDate())
                )
            }
}