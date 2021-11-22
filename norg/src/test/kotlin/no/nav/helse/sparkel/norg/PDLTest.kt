package no.nav.helse.sparkel.norg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PDLTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `svar med alle felter tolkes`() {
        val expected = Person("OLA", "ANDRE", "NORDMANN", LocalDate.parse("2019-11-30"), Kjønn.Mann)
        val actual = objectMapper.readTree(medMellomnavn).toPerson()
        assertEquals(expected, actual)
    }

    @Test
    fun `svar uten mellomnavn tolkes`() {
        val expected = Person("OLA", null, "NORDMANN", LocalDate.parse("2019-11-30"), Kjønn.Mann)
        val actual = objectMapper.readTree(utenMellomnavn).toPerson()
        assertEquals(expected, actual)
    }

    private val medMellomnavn = """
        {
           "data": {
              "hentPerson": {
                 "navn": {
                    "fornavn": "OLA",
                    "mellomnavn": "ANDRE",
                    "etternavn": "NORDMANN"
                 },
                 "foedsel": {
                    "foedselsdato": "2019-11-30"
                 },
                 "kjoenn": {
                    "kjoenn": "MANN"
                 }
              }
           }
        }
    """.trimIndent()

    private val utenMellomnavn = """
        {
           "data": {
              "hentPerson": {
                 "navn": {
                    "fornavn": "OLA",
                    "etternavn": "NORDMANN"
                 },
                 "foedsel": {
                    "foedselsdato": "2019-11-30"
                 },
                 "kjoenn": {
                    "kjoenn": "MANN"
                 }
              }
           }
        }
    """.trimIndent()
}