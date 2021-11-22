package no.nav.helse.sparkel.norg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PDLTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `hentPerson med alle felter`() {
        val expected = Person("OLA", "ANDRE", "NORDMANN", LocalDate.parse("2019-11-30"), Kjønn.Mann, Adressebeskyttelse.STRENGT_FORTROLIG)
        val actual = objectMapper.readTree(medMellomnavn).toPerson()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentPerson uten mellomnavn`() {
        val expected = Person("OLA", null, "NORDMANN", LocalDate.parse("2019-11-30"), Kjønn.Mann, Adressebeskyttelse.STRENGT_FORTROLIG)
        val actual = objectMapper.readTree(utenMellomnavn).toPerson()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentGeografiskTilknytning med alle felter`() {
        val expected = GeografiskTilknytning("OSLO", "030102")
        val actual = objectMapper.readTree(geoTilknytningMedAlt).toGeotilknytning()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentGeografiskTilknytning uten bydel`() {
        val expected = GeografiskTilknytning("OSLO", null)
        val actual = objectMapper.readTree(geoTilknytningUtenBydel).toGeotilknytning()
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
                 },
                 "adressebeskyttelse": {
                    "gradering": "STRENGT_FORTROLIG"
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
                 },
                 "adressebeskyttelse": {
                    "gradering": "STRENGT_FORTROLIG"
                 }
              }
           }
        }
    """.trimIndent()

    private val geoTilknytningMedAlt = """
        {
          "data": {
            "hentGeografiskTilknytning":{
              "gtType": "BYDEL",
              "gtLand": null,
              "gtKommune": "OSLO",
              "gtBydel": "030102"
            }
          }
        }
    """.trimIndent()

    private val geoTilknytningUtenBydel = """
        {
          "data": {
            "hentGeografiskTilknytning":{
              "gtType": "BYDEL",
              "gtLand": null,
              "gtKommune": "OSLO"
            }
          }
        }
    """.trimIndent()
}