package no.nav.helse.sparkel.norg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PDLTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `hentPerson med alle felter`() {
        val expected = Person("OLA", "ANDRE", "NORDMANN", LocalDate.parse("2019-11-30"), Kjønn.Mann, Adressebeskyttelse.STRENGT_FORTROLIG)
        val actual = objectMapper.readTree(medMellomnavn).asPerson()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentPerson uten mellomnavn`() {
        val expected = Person("OLA", null, "NORDMANN", LocalDate.parse("2019-11-30"), Kjønn.Mann, Adressebeskyttelse.STRENGT_FORTROLIG)
        val actual = objectMapper.readTree(utenMellomnavn).asPerson()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentPerson  - strengeste adressebeskyttelse velges`() {
        val expected = Person("LEALAUS", null, "TØFFELDYR", LocalDate.parse("1950-10-27"), Kjønn.Kvinne, Adressebeskyttelse.STRENGT_FORTROLIG)
        val actual = objectMapper.readTree(personMedFlereAdressebeskyttelser).asPerson()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentGeografiskTilknytning - mest nøyaktig velges - bydel`() {
        val expected = "BYDELEN"
        val actual = objectMapper.readTree(geoTilknytningMedAlt).asGeotilknytning().mestNøyaktig()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentGeografiskTilknytning - mest nøyaktig velges - kommune`() {
        val expected = "3403"
        val actual = objectMapper.readTree(geoTilknytningUtenBydel).asGeotilknytning().mestNøyaktig()
        assertEquals(expected, actual)
    }

    @Test
    fun `hentGeografiskTilknytning - mest nøyaktig velges - land`() {
        val expected = "LANDET"
        val actual = objectMapper.readTree(geoTilknytningMedBareLand).asGeotilknytning().mestNøyaktig()
        assertEquals(expected, actual)
    }

    @Test
    fun `graphql queries kan ikke ha linjeskift`() {
        val expected = "aaa bbb ccc"
        val actual = "aaa\nbbb\nccc".onOneLine()
        assertEquals(expected, actual)
    }

    @Language("json")
    private val medMellomnavn = """
        {
           "data": {
              "hentPerson": {
                 "navn": [{
                    "fornavn": "OLA",
                    "mellomnavn": "ANDRE",
                    "etternavn": "NORDMANN"
                 }],
                 "foedsel": [{
                    "foedselsdato": "2019-11-30"
                 }],
                 "kjoenn": [{
                    "kjoenn": "MANN"
                 }],
                 "adressebeskyttelse": [
                    {
                      "gradering": "STRENGT_FORTROLIG"
                    }
                ]
              }
           }
        }
    """.trimIndent()

    @Language("json")
    private val utenMellomnavn = """
        {
           "data": {
              "hentPerson": {
                 "navn": [{
                    "fornavn": "OLA",
                    "mellomnavn": null,
                    "etternavn": "NORDMANN"
                 }],
                 "foedsel": [{
                    "foedselsdato": "2019-11-30"
                 }],
                 "kjoenn": [{
                    "kjoenn": "MANN"
                 }],
                 "adressebeskyttelse": [
                    {
                      "gradering": "STRENGT_FORTROLIG"
                    }
                ]
              }
           }
        }
    """.trimIndent()

    @Language("json")
    private val geoTilknytningMedAlt = """
        {
          "data": {
            "hentGeografiskTilknytning":{
              "gtType": "BYDEL",
              "gtLand": "LANDET",
              "gtKommune": "KOMMUNEN",
              "gtBydel": "BYDELEN"
            }
          }
        }
    """.trimIndent()

    @Language("json")
    private val geoTilknytningUtenBydel = """
        {"data":{"hentGeografiskTilknytning":{"gtLand":null,"gtKommune":"3403","gtBydel":null}}}
    """.trimIndent()

    @Language("json")
    private val geoTilknytningMedBareLand = """
        {
          "data": {
            "hentGeografiskTilknytning":{
              "gtLand": "LANDET",
              "gtKommune": null,
              "gtBydel": null
            }
          }
        }
    """.trimIndent()

    @Language("json")
    private val personMedFlereAdressebeskyttelser = """
        {"data": 
            {
              "hentPerson": {
                "navn":[
                    { 
                      "fornavn":"LEALAUS",
                      "mellomnavn": null,
                      "etternavn":"TØFFELDYR"
                    }
              ],
              "foedsel": [{"foedselsdato":"1950-10-27"}],
              "kjoenn": [{"kjoenn":"KVINNE"}],
              "adressebeskyttelse": [{"gradering": "FORTROLIG"},{"gradering": "STRENGT_FORTROLIG"}]
              }
            }
        }
    """

}