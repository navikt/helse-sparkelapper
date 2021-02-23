package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import no.nav.helse.sparkel.aareg.util.KodeverkClient
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.UstrukturertNavn
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonRequest

class OrganisasjonClient(
    private val organisasjonV5: OrganisasjonV5,
    private val kodeverkClient: KodeverkClient
) {
    fun finnOrganisasjon(orgnr: String): OrganisasjonDto =
        organisasjonV5.hentOrganisasjon(hentOrganisasjonRequst(orgnr)).organisasjon.let { organisasjon ->
            OrganisasjonDto(
                navn = (organisasjon.navn as UstrukturertNavn).navnelinje.filter { it.isNotBlank() }.joinToString(),
                bransjer = organisasjon.organisasjonDetaljer.naering.map { næring ->
                    kodeverkClient.getNæring(næring.naeringskode.kodeRef)
                }
            )
        }

    private fun hentOrganisasjonRequst(orgnr: String) =
        HentOrganisasjonRequest().apply {
            orgnummer = orgnr
            isInkluderHierarki = true
            isInkluderHistorikk = true
        }
}
