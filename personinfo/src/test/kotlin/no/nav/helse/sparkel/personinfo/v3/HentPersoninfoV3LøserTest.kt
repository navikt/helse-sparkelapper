package no.nav.helse.sparkel.personinfo.v3

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert


internal class HentPersoninfoV3LøserTest {
    private val rapid = TestRapid()

    @BeforeEach
    fun reset() {
        rapid.reset()
    }

    @Test
    fun `henter aktørid og fødselsdato`() {
        val pdl = PDL { _, _, _ -> aktørIdOgFødselsdatoPdlReply }
        HentPersoninfoV3Løser(rapid, pdl)
        rapid.sendTestMessage(behov("aktørId", "fødselsdato"))
        assertEquals(1, rapid.inspektør.size)
        val løsning = rapid.inspektør.message(0).path("@løsning").path("HentPersoninfoV3")

        @Language("JSON")
        val forventet = """
            {
              "aktørId": "1234567890123",
              "fødselsdato": "1980-04-09"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `henter rubb & stubb`() {
        val pdl = PDL { _, _, _ -> rubbOgStubbPdlReply }
        HentPersoninfoV3Løser(rapid, pdl)
        rapid.sendTestMessage(behov(*Attributt.values().map { it.name }.toTypedArray()))
        assertEquals(1, rapid.inspektør.size)
        val løsning = rapid.inspektør.message(0).path("@løsning").path("HentPersoninfoV3")

        @Language("JSON")
        val forventet = """
            {
              "aktørId": "1234567890123",
              "folkeregisterident": "12345678901",
              "historiskeFolkeregisteridenter": ["02345678901"],
              "fødselsdato": "1980-04-09",
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "kjønn": "Kvinne",
              "adressebeskyttelse": "Fortrolig",
              "støttes": true,
              "dødsdato": "2001-07-08"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `henter adressebeskyttelse når man forespør adressebeskyttelse`() {
        val pdl = PDL { _, _, _ -> adresseBeskyttelseReply("STRENGT_FORTROLIG") }
        HentPersoninfoV3Løser(rapid, pdl)
        rapid.sendTestMessage(behov("adressebeskyttelse"))
        assertEquals(1, rapid.inspektør.size)
        val løsning = rapid.inspektør.message(0).path("@løsning").path("HentPersoninfoV3")

        @Language("JSON")
        val forventet = """
            {
              "adressebeskyttelse": "StrengtFortrolig"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `støtter ikke personer med adressebeskyttelse strengt_fortrolig_utland`() {
        testStøtteAvAdressebeskyttelse("STRENGT_FORTROLIG_UTLAND", false)
    }

    @Test
    fun `støtter ikke personer med adressebeskyttelse strengt_fortrolig`() {
        testStøtteAvAdressebeskyttelse("STRENGT_FORTROLIG", false)
    }

    @Test
    fun `støtter personer med adressebeskyttelse fortrolig`() {
        testStøtteAvAdressebeskyttelse("FORTROLIG", true)
    }

    @Test
    fun `støtter personer med adressebeskyttelse ugradert`() {
        testStøtteAvAdressebeskyttelse("UGRADERT", true)
    }

    @Test
    fun `støtter personer med adressebeskyttelse ukjent`() {
        testStøtteAvAdressebeskyttelse("UKJENT", true)
    }

    private fun testStøtteAvAdressebeskyttelse(adressebeskyttelse: String, støttes: Boolean) {
        val pdl = PDL { _, _, _ -> adresseBeskyttelseReply(adressebeskyttelse) }
        HentPersoninfoV3Løser(rapid, pdl)
        rapid.sendTestMessage(behov("støttes"))
        assertEquals(1, rapid.inspektør.size)
        val løsning = rapid.inspektør.message(0).path("@løsning").path("HentPersoninfoV3")

        @Language("JSON")
        val forventet = """
                {
                  "støttes": $støttes
                }
            """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()

        @Language("JSON")
        private fun behov(vararg behov: String) = """
        {
          "@id": "${UUID.randomUUID()}",
          "@behovId": "${UUID.randomUUID()}",
          "@behov": ["HentPersoninfoV3"],
          "HentPersoninfoV3": {
            "ident": "11111112345",
            "attributter": ${behov.map { "\"$it\"" }}
          }
        }
        """.trimIndent()

        @Language("JSON")
        private val aktørIdOgFødselsdatoPdlReply = """
        {
          "data": {
            "hentIdenter": {
              "identer": [
                {
                  "ident": "12345678901",
                  "gruppe": "FOLKEREGISTERIDENT"
                },
                {
                  "ident": "1234567890123",
                  "gruppe": "AKTORID"
                }
              ]
            },
            "hentPerson": {
              "foedselsdato": [
                {
                  "foedselsdato": "1980-04-09"
                }
              ]
            }
          }
        }
        """.let { objectMapper.readTree(it) }

        @Language("JSON")
        private val rubbOgStubbPdlReply = """
        {
          "data": {
            "hentIdenter": {
              "identer": [
                {
                  "ident": "12345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": false
                },
                {
                  "ident": "02345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": true
                },
                {
                  "ident": "1234567890123",
                  "gruppe": "AKTORID",
                  "historisk": false
                }
              ]
            },
            "hentPerson": {
              "navn": [
                {
                  "fornavn": "LITEN",
                  "mellomnavn": null,
                  "etternavn": "TRANFLASKE"
                }
              ],
              "foedselsdato": [
                {
                  "foedselsdato": "1980-04-09"
                }
              ],
              "adressebeskyttelse": [
                {
                  "gradering": "FORTROLIG"
                }
              ],
              "kjoenn": [
                {
                  "kjoenn": "KVINNE"
                }
              ],
              "doedsfall": [
                {
                  "doedsdato": "2001-07-08",
                  "metadata": {
                    "master": "Freg"
                  }
                }
              ]
            }
          }
        }
        """.let { objectMapper.readTree(it) }

        @Language("JSON")
        private fun adresseBeskyttelseReply(adressebeskyttelse: String) = """
        {
          "data": {
            "hentIdenter": {
              "identer": [
                {
                  "ident": "12345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": false
                },
                {
                  "ident": "02345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": true
                },
                {
                  "ident": "1234567890123",
                  "gruppe": "AKTORID",
                  "historisk": false
                }
              ]
            },
            "hentPerson": {
              "adressebeskyttelse": [
                {
                  "gradering": "$adressebeskyttelse"
                }
              ]
            }
          }
        }
        """.let { objectMapper.readTree(it) }
    }
}
