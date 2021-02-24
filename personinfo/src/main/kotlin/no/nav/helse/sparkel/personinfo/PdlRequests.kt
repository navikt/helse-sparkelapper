package no.nav.helse.sparkel.personinfo

data class PdlQueryObject(
        val query: String,
        val variables: Variables
)

data class Variables(
        val ident: String
)
