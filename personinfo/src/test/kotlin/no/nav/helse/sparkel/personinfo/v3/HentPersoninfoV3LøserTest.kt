package no.nav.helse.sparkel.personinfo.v3

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private val objectMapper = jacksonObjectMapper()

internal class HentPersoninfoV3LøserTest {
    private val rapid = TestRapid()

    @Test
    fun `henter aktørid og fødselsdato`() {
        val pdl = object : HentPersoninfoV3PDLClient {
            override fun hent(ident: String, callId: String, attributter: Set<Attributt>) = komplettSvar
        }
        HentPersoninfoV3Løser(rapid, pdl)
        rapid.sendTestMessage(behov)
        assertEquals(1, rapid.inspektør.size)
        val løsning = rapid.inspektør.message(0).path("@løsning")
        assertEquals("1234567890123", løsning["HentPersoninfoV3"]["aktørId"].asText())
        assertEquals("1980-04-09", løsning["HentPersoninfoV3"]["fødselsdato"].asText())
    }

}

@Language("JSON")
private val behov = """
    {
      "@id": "${UUID.randomUUID()}",
      "@behovId": "${UUID.randomUUID()}",
      "@behov": ["HentPersoninfoV3"],
      "HentPersoninfoV3": {
        "ident": "11111112345",
        "attributter": ["fødselsdato", "aktørId"]
      }
    }
""".trimIndent()

@Language("JSON")
private val komplettSvar = """
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