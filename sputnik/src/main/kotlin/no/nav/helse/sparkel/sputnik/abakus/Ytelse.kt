package no.nav.helse.sparkel.sputnik.abakus

internal class Ytelse(private val dto: String) {
    override fun toString() = dto
    override fun hashCode() = dto.hashCode()
    override fun equals(other: Any?) = other is Ytelse && other.dto == dto
}