package no.nav.helse.sparkel.aareg.arbeidsforhold

import no.nav.helse.sparkel.aareg.util.KodeverkClient
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import javax.xml.datatype.DatatypeConstants
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

class ArbeidsforholdClient(
    private val arbeidsforholdV3: ArbeidsforholdV3,
    private val kodeverkClient: KodeverkClient
) {
    fun finnArbeidsforhold(
        organisasjonsnummer: String,
        aktørId: String,
        fom: LocalDate,
        tom: LocalDate,
    ): List<ArbeidsforholdDto> =
        arbeidsforholdV3.finnArbeidsforholdPrArbeidstaker(
            hentArbeidsforholdRequest(aktørId, fom, tom)
        )
            .arbeidsforhold
            .toList()
            .filter { it.arbeidsgiver is Organisasjon && (it.arbeidsgiver as Organisasjon).orgnummer == organisasjonsnummer }
            .flatMap { it.arbeidsavtale }
            .map {
                ArbeidsforholdDto(
                    startdato = it.fomBruksperiode.toLocalDate(),
                    sluttdato = it.tomBruksperiode?.toLocalDate(),
                    stillingsprosent = it.stillingsprosent.toInt(),
                    stillingstittel = kodeverkClient.getYrke(it.yrke.kodeRef)
                )
            }

    private fun hentArbeidsforholdRequest(aktørId: String, fom: LocalDate, tom: LocalDate) =
        FinnArbeidsforholdPrArbeidstakerRequest().apply {
            ident = NorskIdent().apply {
                ident = aktørId
            }
            arbeidsforholdIPeriode = Periode().apply {
                this.fom = fom.toXmlGregorianCalendar()
                this.tom = tom.toXmlGregorianCalendar()
            }
            rapportertSomRegelverk = Regelverker().apply {
                value = "A_ORDNINGEN"
                kodeRef = "A_ORDNINGEN"
            }
        }

    private val datatypeFactory = DatatypeFactory.newInstance()

    private fun LocalDate.toXmlGregorianCalendar() = this.let {
        val gcal = GregorianCalendar.from(this.atStartOfDay(ZoneOffset.UTC))
        datatypeFactory.newXMLGregorianCalendar(gcal)
    }

    private fun XMLGregorianCalendar.toLocalDate() =
        LocalDate.of(year, month, if (day == DatatypeConstants.FIELD_UNDEFINED) 1 else day)
}
