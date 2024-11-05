package no.nav.helse.sparkel.sputnik

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import java.util.UUID
import no.nav.helse.sparkel.sputnik.abakus.Ytelse
import no.nav.helse.sparkel.sputnik.abakus.Stønadsperiode

internal class Stønad private constructor(
    private val behov: String,
    fomKey: String,
    tomKey: String,
    private val abakusYtelser: Set<Ytelse>,
    private val løsning: (stønadsperioder: Set<Stønadsperiode>) -> Any
) {
    private val fomPath = "$behov.$fomKey"
    private val tomPath = "$behov.$tomKey"

    private fun skalLøses(packet: JsonMessage) = behov in packet.behov

    private fun periode(packet: JsonMessage) = packet[fomPath].asLocalDate() to packet[tomPath].asLocalDate()

    private fun validate(packet: JsonMessage) {
        packet.interestedIn(fomPath)
        packet.interestedIn(tomPath)
    }

    internal fun leggTilLøsning(packet: JsonMessage, stønadsperioder: Set<Stønadsperiode>) {
        val (fom, tom) = periode(packet)
        val aktuelleStønadsperioder = stønadsperioder
            .filter { it.ytelse in abakusYtelser }
            .filterNot { it.fom > tom } // Filtrerer bort perioder som starter etter tom
            .filterNot { it.tom < fom } // Filtrerer bort perioder som slutter før fom
            .toSet()

        packet["@id"] = "${UUID.randomUUID()}"
        packet["@løsning"] = mapOf(behov to løsning(aktuelleStønadsperioder))
    }

    override fun toString() = behov
    override fun equals(other: Any?) = other is Stønad && other.behov == behov
    override fun hashCode() = behov.hashCode()

    internal companion object {
        private val ForeldrepengerAbakusYtelse = Ytelse("FORELDREPENGER")
        private val SvangerskapspengerAbakusYtelse = Ytelse("SVANGERSKAPSPENGER")
        private val Foreldrepenger = Stønad(
            behov = "Foreldrepenger",
            fomKey = "foreldrepengerFom",
            tomKey = "foreldrepengerTom",
            abakusYtelser = setOf(ForeldrepengerAbakusYtelse, SvangerskapspengerAbakusYtelse)
        ) { stønadsperioder ->
            mapOf(
                "Foreldrepengeytelse" to stønadsperioder.foreldrepengerOgSvangerskapspengerYtelse(ForeldrepengerAbakusYtelse),
                "Svangerskapsytelse" to stønadsperioder.foreldrepengerOgSvangerskapspengerYtelse(SvangerskapspengerAbakusYtelse)
            )
        }

        private fun Set<Stønadsperiode>.foreldrepengerOgSvangerskapspengerYtelse(ytelse: Ytelse): Any? {
            val aktuelle = filter { it.ytelse == ytelse }.takeUnless { it.isEmpty() } ?: return null
            return mapOf(
                "fom" to "${aktuelle.minOf { it.fom }}",
                "tom" to "${aktuelle.maxOf { it.tom }}",
                "vedtatt" to "${aktuelle.first().vedtatt}",
                "perioder" to aktuelle.map { mapOf(
                    "fom" to "${it.fom}",
                    "tom" to "${it.tom}",
                    "grad" to "${it.grad}"
                )}
            )
        }

        private val Pleiepenger = Stønad(
            behov = "Pleiepenger",
            fomKey = "pleiepengerFom",
            tomKey = "pleiepengerTom",
            abakusYtelser = setOf(Ytelse("PLEIEPENGER_SYKT_BARN"), Ytelse("PLEIEPENGER_NÆRSTÅENDE"))
        ) { stønadsperioder -> stønadsperioder.barnSykdomLøsning() }

        private val Omsorgspenger = Stønad(
            behov = "Omsorgspenger",
            fomKey = "omsorgspengerFom",
            tomKey = "omsorgspengerTom",
            abakusYtelser = setOf(Ytelse("OMSORGSPENGER"))
        ) { stønadsperioder -> stønadsperioder.barnSykdomLøsning() }

        private val Opplæringspenger = Stønad(
            behov = "Opplæringspenger",
            fomKey = "opplæringspengerFom",
            tomKey = "opplæringspengerTom",
            abakusYtelser = setOf(Ytelse("OPPLÆRINGSPENGER"))
        ) { stønadsperioder -> stønadsperioder.barnSykdomLøsning() }

        private fun Set<Stønadsperiode>.barnSykdomLøsning() = map { mapOf(
            "fom" to "${it.fom}",
            "tom" to "${it.tom}",
            "grad" to it.grad
        )}

        private val AlleStønader = setOf(Foreldrepenger, Pleiepenger, Omsorgspenger, Opplæringspenger)
        private val JsonMessage.behov get() = get("@behov").map { it.asText() }

        internal fun stønaderSomSkalLøses(packet: JsonMessage) = AlleStønader.filter { it.skalLøses(packet) }
        internal fun harRelevanteBehov(packet: JsonMessage) {
            if (stønaderSomSkalLøses(packet).isEmpty()) throw RuntimeException("Behovet er ikke relevant")
        }
        internal fun Iterable<Stønad>.abakusYtelser() = flatMap { it.abakusYtelser }.toTypedArray()
        internal fun Iterable<Stønad>.omsluttendePeriode(packet: JsonMessage) = map { it.periode(packet) }.let { allePerioder ->
            allePerioder.minOf { it.first } to allePerioder.maxOf { it.second }
        }
        internal fun validate(packet: JsonMessage) = AlleStønader.forEach { it.validate(packet) }
    }
}