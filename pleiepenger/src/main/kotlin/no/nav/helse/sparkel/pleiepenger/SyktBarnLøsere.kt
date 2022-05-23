package no.nav.helse.sparkel.pleiepenger

import java.time.LocalDate
import no.nav.helse.rapids_rivers.RapidsConnection

internal class PleiepengerløserV2(
    rapidsConnection: RapidsConnection,
    vararg kilder: SyktBarnKilde
): SyktBarnLøser(rapidsConnection, "Pleiepenger", "pleiepengerFom", "pleiepengerTom", *kilder) {
    override fun stønadsperioder(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate,
        kilde: SyktBarnKilde
    ) = kilde.pleiepenger(fnr, fom, tom)
}

internal class OmsorgspengerløserV2(
    rapidsConnection: RapidsConnection,
    vararg kilder: SyktBarnKilde
): SyktBarnLøser(rapidsConnection, "Omsorgspenger", "omsorgspengerFom", "omsorgspengerTom", *kilder) {
    override fun stønadsperioder(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate,
        kilde: SyktBarnKilde
    ) = kilde.omsorgspenger(fnr, fom, tom)
}

internal class OpplæringspengerløserV2(
    rapidsConnection: RapidsConnection,
    vararg kilder: SyktBarnKilde
): SyktBarnLøser(rapidsConnection, "Opplæringspenger", "opplæringspengerFom", "opplæringspengerTom", *kilder) {
    override fun stønadsperioder(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate,
        kilde: SyktBarnKilde
    ) = kilde.opplæringspenger(fnr, fom, tom)
}
