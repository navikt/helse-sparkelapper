package no.nav.helse.sparkel.abakus

import net.logstash.logback.argument.StructuredArguments.keyValue

sealed class Identifiktor(private val verdi: String, key: String) {
    internal val keyValue = keyValue(key, verdi)
    override fun toString() = verdi
}
class AktørId private constructor(verdi: String): Identifiktor(verdi, "aktørId") {
    companion object {
        val String.aktørId get() = AktørId(this)
    }
}
class Fødselsnummer private constructor(verdi: String): Identifiktor(verdi, "fødselsnummer") {
    companion object {
        val String.fødselsnummer get() = Fødselsnummer(this)
    }
}
