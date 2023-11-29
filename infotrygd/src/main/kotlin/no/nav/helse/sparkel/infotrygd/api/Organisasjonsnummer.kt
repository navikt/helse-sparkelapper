package no.nav.helse.sparkel.infotrygd.api

class Organisasjonsnummer private constructor(private val id: String) {
    init {
        check(id.gyldig) { "Ugyldig organisasjonsnummer" }
    }

    override fun toString() = id

    companion object {
        private val regex = "\\d{9}".toRegex()
        private val String.gyldig get() = matches(regex)
        val String.organisasjosnummerOrNull get() = takeIf { gyldig }?.let { Organisasjonsnummer(this) }
    }
}