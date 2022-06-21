package no.nav.helse.sparkel.identer

data class AktørV2 (
    val identifikatorer: List<Identifikator>,
    val key: String,
) {
    fun gjeldendeFolkeregisterident() = identifikatorer.singleOrNull {
        it.gjeldende && it.type == Type.FOLKEREGISTERIDENT
    }?.idnummer
}

data class Identifikator(
    val idnummer: String,
    val type: Type,
    val gjeldende: Boolean
)

enum class Type {
    FOLKEREGISTERIDENT,
    AKTORID,
    NPID
}