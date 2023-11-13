package no.nav.helse.sparkel.infotrygd.api

class Personidentifikator(private val id: String) {
    init {
        check(id.matches("\\d".toRegex())) { "Ugyldig Personidentifikator" }
    }
    override fun toString() = id
}