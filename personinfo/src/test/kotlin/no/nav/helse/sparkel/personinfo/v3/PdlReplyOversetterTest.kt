package no.nav.helse.sparkel.personinfo.v3

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert

internal class PdlReplyOversetterTest {

    @Test
    fun `response fra pdl inneholder alle etterspurte attributter`() {
        val løsning = PdlReplyOversetter.oversett(komplettSvar, setOf(Attributt.aktørId, Attributt.fødselsdato, Attributt.folkeregisterident))
        @Language("JSON")
        val forventet = """
            {
              "aktørId": "1234567890123",
              "folkeregisterident": "12345678901",
              "fødselsdato": "1980-04-09"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `response fra pdl som mangler fødselsdato`() {
        assertThrows<IllegalStateException> { PdlReplyOversetter.oversett(manglerFødselsdato, setOf(Attributt.fødselsdato)) }
        val løsning = PdlReplyOversetter.oversett(manglerFødselsdato, setOf(Attributt.aktørId, Attributt.folkeregisterident))
        @Language("JSON")
        val forventet = """
            {
              "aktørId": "1234567890123",
              "folkeregisterident": "12345678901"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `response fra pdl som mangler aktørId`() {
        assertThrows<IllegalStateException> { PdlReplyOversetter.oversett(manglerAktørId, setOf(Attributt.aktørId)) }
        val løsning = PdlReplyOversetter.oversett(manglerAktørId, setOf(Attributt.fødselsdato, Attributt.folkeregisterident))
        @Language("JSON")
        val forventet = """
            {
              "folkeregisterident": "12345678901",
              "fødselsdato": "1980-04-09"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `response fra pdl som mangler folkeregisterident`() {
        assertThrows<IllegalStateException> { PdlReplyOversetter.oversett(manglerFolkeregisterident, setOf(Attributt.folkeregisterident)) }
        val løsning = PdlReplyOversetter.oversett(manglerFolkeregisterident, setOf(Attributt.fødselsdato, Attributt.aktørId))
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
    fun `response fra pdl med historiske identer`() {
        val løsning = PdlReplyOversetter.oversett(historiskeIdenter, setOf(Attributt.aktørId, Attributt.folkeregisterident, Attributt.historiskeFolkeregisteridenter))
        @Language("JSON")
        val forventet = """
            {
              "aktørId": "2234567890123",
              "folkeregisterident": "32345678901",
              "historiskeFolkeregisteridenter": [
                "12345678901",
                "22345678901"
              ]
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `navn på person med mellomnavn`() {
        val løsning = PdlReplyOversetter.oversett(navn("mellomnavnsen"), setOf(Attributt.navn))
        @Language("JSON")
        val forventet = """
            {
              "fornavn": "LITEN",
              "mellomnavn": "mellomnavnsen",
              "etternavn": "TRANFLASKE"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `navn på person uten mellomnavn`() {
        val løsning = PdlReplyOversetter.oversett(navn(null), setOf(Attributt.navn))
        @Language("JSON")
        val forventet = """
            {
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE"
            }
        """
        JSONAssert.assertEquals(forventet, "$løsning", true)
    }

    @Test
    fun `error response fra pdl`() {
        assertThrows<RuntimeException> { PdlReplyOversetter.oversett(error, setOf(Attributt.folkeregisterident, Attributt.fødselsdato, Attributt.aktørId)) }
    }

    internal companion object {
        private val objectMapper = jacksonObjectMapper()

        @Language("JSON")
        internal val komplettSvar = """
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
                  "ident": "1234567890123",
                  "gruppe": "AKTORID",
                  "historisk": false
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
        private val manglerFødselsdato = """
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
                  "ident": "1234567890123",
                  "gruppe": "AKTORID",
                  "historisk": false
                }
              ]
            }
          }
        }
        """.let { objectMapper.readTree(it) }

        @Language("JSON")
        private val manglerAktørId = """
        {
          "data": {
            "hentIdenter": {
              "identer": [
                {
                  "ident": "12345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": false
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
        private val manglerFolkeregisterident = """
        {
          "data": {
            "hentIdenter": {
              "identer": [
                {
                  "ident": "1234567890123",
                  "gruppe": "AKTORID",
                  "historisk": false
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
        private val historiskeIdenter = """
        {
          "data": {
            "hentIdenter": {
              "identer": [
                {
                  "ident": "12345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": true
                },
                            {
                  "ident": "22345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": true
                },
                {
                  "ident": "1234567890123",
                  "gruppe": "AKTORID",
                  "historisk": true
                },
                {
                  "ident": "32345678901",
                  "gruppe": "FOLKEREGISTERIDENT",
                  "historisk": false
                },
                {
                  "ident": "2234567890123",
                  "gruppe": "AKTORID",
                  "historisk": false
                }
              ]
            }
          }
        }
        """.let { objectMapper.readTree(it) }

        @Language("JSON")
        private fun navn(mellomnavn: String?) = """
        {
          "data": {
            "hentPerson": {
              "navn": [
                {
                  "fornavn": "LITEN",
                  "mellomnavn": ${mellomnavn?.let { "\"$it\""}}, 
                  "etternavn": "TRANFLASKE"
                }
              ]
            }
          }
        }
        """.let { objectMapper.readTree(it) }

        @Language("JSON")
        private val error = """
        {
          "errors": ["noe er gæli"]
        }
        """.let { objectMapper.readTree(it) }
    }
}