package no.nav.helse.sparkel.forsikring

@JvmInline
value class Fnr(val fnr: String) {
    init {
        require(fnr.length == 11) { "Fødselsnummer skal være 11 tegn, var ${fnr.length}" }
    }
}

internal fun Fnr.date() = this.fnr.substring(0,2)
internal fun Fnr.month(): String = this.fnr.substring(2, 4)
internal fun Fnr.year(): String = this.fnr.substring(4, 6)
internal fun Fnr.id(): String = this.fnr.substring(6)

internal fun Fnr.formatAsITFnr() = "${this.year()}${this.month()}${this.date()}${this.id()}"
