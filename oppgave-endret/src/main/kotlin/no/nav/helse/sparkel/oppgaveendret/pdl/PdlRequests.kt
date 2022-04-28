package no.nav.helse.sparkel.oppgaveendret.pdl

data class PdlQueryObject(
    val query: String,
    val variables: Variables
)

data class Variables(
    val ident: String
)
