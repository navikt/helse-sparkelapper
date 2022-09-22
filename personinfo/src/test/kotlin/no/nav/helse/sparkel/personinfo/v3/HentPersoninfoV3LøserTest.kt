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
              "dødsdato": "2001-07-08"
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
              "foedsel": [
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
              "foedsel": [
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
    }
}
