package no.nav.helse.sparkel.personinfo.v3

import no.nav.helse.sparkel.personinfo.v3.PdlQueryBuilder.Companion.formaterQuery
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PdlQueryBuilderTest {

    @Test
    fun `etterspør alle støttede attributter`() {
        val query = PdlQueryBuilder(Attributt.values().toSet()).build()
        @Language("GraphQL")
        val forventet = """
            query(${'$'}ident: ID!) { 
                hentIdenter(ident: ${'$'}ident, historikk: true, grupper: [AKTORID, FOLKEREGISTERIDENT]) { 
                    identer { ident, gruppe, historisk } 
                }
                hentPerson(ident: ${'$'}ident) { 
                    foedselsdato { foedselsdato }, 
                    navn(historikk: false) { fornavn, mellomnavn, etternavn }, 
                    adressebeskyttelse(historikk: false) { gradering }, 
                    kjoenn(historikk: false) { kjoenn }, 
                    doedsfall { doedsdato, metadata { master } } 
                } 
            }
        """.formaterQuery()
        assertEquals(forventet, query)
    }

    @Test
    fun `etterspør fødselsdato og aktørId`() {
        val query = PdlQueryBuilder(setOf(Attributt.aktørId, Attributt.fødselsdato)).build()
        @Language("GraphQL")
        val forventet = """
            query(${'$'}ident: ID!) { 
                hentIdenter(ident: ${'$'}ident, historikk: false, grupper: [AKTORID]) { 
                    identer { ident, gruppe, historisk } 
                }
                hentPerson(ident: ${'$'}ident) { 
                    foedselsdato { foedselsdato }
                }
            }
        """.formaterQuery()
        assertEquals(forventet, query)
    }

    @Test
    fun `etterspør attributter som hentes i HentPersoninfoV2`() {
        val query = PdlQueryBuilder(setOf(
            Attributt.fødselsdato,
            Attributt.navn,
            Attributt.adressebeskyttelse,
            Attributt.kjønn
        )).build()
        @Language("GraphQL")
        val forventet = """
            query(${'$'}ident: ID!) {
                hentPerson(ident: ${'$'}ident) { 
                    foedselsdato { foedselsdato }, 
                    navn(historikk: false) { fornavn, mellomnavn, etternavn }, 
                    adressebeskyttelse(historikk: false) { gradering }, 
                    kjoenn(historikk: false) { kjoenn }
                } 
            }
        """.formaterQuery()
        assertEquals(forventet, query)
    }

    @Test
    fun `etterspør dødsinfo`() {
        val query = PdlQueryBuilder(setOf(Attributt.dødsdato)).build()
        @Language("GraphQL")
        val forventet = """
            query(${'$'}ident: ID!) {
                hentPerson(ident: ${'$'}ident) { 
                    doedsfall { doedsdato, metadata { master } } 
                } 
            }
        """.formaterQuery()
        assertEquals(forventet, query)
    }

    @Test
    fun `ingen etterspurte attributter`() {
        assertThrows<IllegalArgumentException> {
            PdlQueryBuilder(emptySet())
        }
    }

    @Test
    fun `etterspør støttes`() {
        val query = PdlQueryBuilder(setOf(Attributt.støttes)).build()
        @Language("GraphQL")
        val forventet = """
            query(${'$'}ident: ID!) {
                hentPerson(ident: ${'$'}ident) { 
                    adressebeskyttelse(historikk: false) { gradering }
                } 
            }
        """.formaterQuery()
        assertEquals(forventet, query)
    }
}