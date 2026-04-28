package no.nav.helse.sparkel.forsikring

@JvmInline
value class Fnr(val fnr: String) {
    init {
        require(fnr.length == 11) { "Fødselsnummer skal være 11 tegn, var ${fnr.length}" }
    }

    fun date() = fnr.substring(0, 2)
    fun month() = fnr.substring(2, 4)
    fun year() = fnr.substring(4, 6)
    fun id() = fnr.substring(6)
}

internal fun Fnr.formatAsITFnr() = "${year()}${month()}${date()}${id()}"
