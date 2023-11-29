package no.nav.helse.sparkel.infotrygd.api

class Personidentifikator(private val id: String) {
    init {
        check(id.matches("\\d{11}".toRegex())) { "Ugyldig Personidentifikator $id" }
    }
    override fun toString() = id

    override fun equals(other: Any?) = other is Personidentifikator && other.id == this.id
}