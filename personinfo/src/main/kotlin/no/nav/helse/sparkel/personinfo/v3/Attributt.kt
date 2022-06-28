package no.nav.helse.sparkel.personinfo.v3

internal enum class Attributt {
    aktørId,
    folkeregisterident,
    fødselsdato,
    historiskeFolkeregisteridenter,
    navn,
    adressebeskyttelse,
    kjønn,
    dødsdato;

    internal companion object {
        internal fun fromString(name: String) = values().firstOrNull { it.name == name }
    }
}