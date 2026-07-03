package no.nav.helse.sparkel.institusjonsopphold

import java.time.LocalDate
import tools.jackson.databind.JsonNode

class Institusjonsoppholdperiode(jsonNode: JsonNode) {
    val kategori = jsonNode["kategori"].asEnumValue<Oppholdstype>()
    val startdato = jsonNode["startdato"].stringValue().let { LocalDate.parse(it) }
    val faktiskSluttdato = jsonNode["faktiskSluttdato"]?.takeUnless { it.isNull }?.stringValue()?.let { LocalDate.parse(it) }

    internal companion object {
        internal fun List<Institusjonsoppholdperiode>.filtrer(fom: LocalDate, tom: LocalDate) = filter { it.overlapperMed(fom, tom) }
    }

    internal fun overlapperMed(fom: LocalDate, tom: LocalDate) =
        maxOf(startdato, fom) <= (faktiskSluttdato?.let { minOf(it, tom) } ?: tom)

}

enum class Institusjonstype(val beskrivelse: String) {
    AS("Alders- og sykehjem"),
    FO("Fengsel"),
    HS("Helseinstitusjon")
}

enum class Oppholdstype(val beskrivelse: String) {
    A("Alders- og sykehjem"),
    D("Dagpasient"),
    F("Ferieopphold"),
    H("Heldøgnpasient"),
    P("Fødsel"),
    R("Opptreningsinstitusjon"),
    S("Soningsfange"),
    V("Varetektsfange")
}

private inline fun <reified T : Enum<T>> JsonNode?.asEnumValue() = this?.takeUnless { it.isNull }?.stringValue()?.takeUnless { it == "" }?.let { enumValueOf<T>(it) }
