package no.nav.helse.sparkel.oppgaveendret.oppgave


// Det finnes flere felter på klassen Oppgave, men vi velger å bare mappe dei vi trenger
data class Oppgave(
    val id: Long,
    val tema: String? = null,
    val ident: Ident? = null
)

data class Ident(
    val id: Long? = null,
    val identType: IdentType,
    val verdi: String,
    val folkeregisterident: String? = null,
)

enum class IdentType {
    AKTOERID, ORGNR, SAMHANDLERNR, BNR
}

