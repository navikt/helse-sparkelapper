package no.nav.helse.sparkel.sykepengeperioder

internal class Fnr(private val fnr: String) {
    init {
        require(fnr.length == 11) { "Fødselsnummer skal være 11 tegn, var ${fnr.length}" }
    }

    private val date: String = fnr.substring(0, 2)
    private val month: String = fnr.substring(2, 4)
    private val year: String = fnr.substring(4, 6)
    private val id: String = fnr.substring(6)

    internal fun formatAsITFnr() = "$year$month$date$id"

    override fun toString() = fnr
}
