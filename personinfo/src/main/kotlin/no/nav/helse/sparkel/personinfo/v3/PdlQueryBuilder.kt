package no.nav.helse.sparkel.personinfo.v3

import org.intellij.lang.annotations.Language

internal class PdlQueryBuilder(
    private val attributter: Set<Attributt>
) {

    init {
        require(attributter.isNotEmpty()) {
            "Må settes minst en attributt"
        }
    }

    @Language("GraphQL")
    private fun hentIdenter() = """
        hentIdenter(ident: ${DOLLAR}ident, historikk: ${attributter.trengerHentIdenterHistorikk()}, grupper: ${attributter.hentIdenterGrupper()}) {
            identer { ident, gruppe, historisk }
        }
    """.takeIf { attributter.trengerHentIdenter() }

    @Language("GraphQL")
    private fun hentPerson() = """
        hentPerson(ident: ${DOLLAR}ident) {
            ${attributter.mapNotNull { hentPersonElementer[it] }.toSet().joinToString()}
        }
    """.takeIf { attributter.trengerHentPerson() }

    @Language("GraphQL")
    internal fun build() = """
        query(${DOLLAR}ident: ID!) { 
            ${listOfNotNull(hentIdenter(), hentPerson()).joinToString(" ")} 
        }
    """.formaterQuery()

    internal companion object {
        private const val DOLLAR = "$"
        internal fun String.formaterQuery() = replace("[\n\r]".toRegex(), "").replace("\\s{2,}".toRegex(), " ")
        private val trengerHentIdenter = setOf(Attributt.aktørId, Attributt.folkeregisterident, Attributt.historiskeFolkeregisteridenter)
        private fun Set<Attributt>.trengerHentIdenter() = intersect(trengerHentIdenter).isNotEmpty()
        private fun Set<Attributt>.trengerHentIdenterHistorikk() = contains(Attributt.historiskeFolkeregisteridenter)
        private fun Set<Attributt>.trengerHentPerson() = intersect(hentPersonElementer.keys).isNotEmpty()
        private fun Set<Attributt>.hentIdenterGrupper() = mapNotNull { it.identGruppe() }.toSet()

        private fun Attributt.identGruppe() =
            if (this == Attributt.historiskeFolkeregisteridenter) "FOLKEREGISTERIDENT"
            else if (this == Attributt.folkeregisterident) "FOLKEREGISTERIDENT"
            else if (this == Attributt.aktørId) "AKTORID"
            else null

        private val hentPersonElementer = mapOf(
            Attributt.fødselsdato to "foedselsdato { foedselsdato }",
            Attributt.navn to "navn(historikk: false) { fornavn, mellomnavn, etternavn }",
            Attributt.adressebeskyttelse to "adressebeskyttelse(historikk: false) { gradering }",
            Attributt.støttes to "adressebeskyttelse(historikk: false) { gradering }",
            Attributt.kjønn to "kjoenn(historikk: false) { kjoenn }",
            Attributt.dødsdato to "doedsfall { doedsdato, metadata { master } }"
        )
    }
}