package no.nav.helse.sparkel.personinfo

import java.time.LocalDate




/**
 * Tilsvarer graphql-spørringen hentFullPerson.graphql
 */
data class PdlHentPerson(val hentPerson: PdlFullPersonliste?) {

        data class PdlFullPersonliste(
                val doedsfall: List<PdlDoedsfall> ) {

                fun trekkUtDoedsfalldato() = doedsfall.firstOrNull()?.doedsdato

                data class PdlDoedsfall(val doedsdato: LocalDate)
        }
}

data class PdlIdent(val ident: String, val gruppe: PdlIdentGruppe) {
        enum class PdlIdentGruppe { AKTORID, FOLKEREGISTERIDENT, NPID }
}


open class PdlResponse<T>(
        open val errors: List<PdlError>?,
        open val data: T?
)

data class PdlError(
        val message: String,
        val locations: List<PdlErrorLocation>,
        val path: List<String>?,
        val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
        val line: Int?,
        val column: Int?
)

data class PdlErrorExtension(
        val code: String?,
        val classification: String
)
