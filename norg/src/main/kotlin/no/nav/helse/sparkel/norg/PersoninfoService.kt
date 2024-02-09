package no.nav.helse.sparkel.norg

import java.io.IOException
import no.nav.helse.sparkel.retry

class PersoninfoService(private val norg2Client: Norg2Client, private val pdl: PDL) {
    suspend fun finnBehandlendeEnhet(fødselsnummer: String, behovId: String): String {
        val adresseBeskytellse = requireNotNull(finnAdressebeskyttelse(fødselsnummer, behovId)).kode
        val geografiskTilknytning =
            requireNotNull(finnGeografiskTilknytning(fødselsnummer, behovId))
        return norg2Client.finnBehandlendeEnhet(geografiskTilknytning.mestNøyaktig(), adresseBeskytellse).enhetNr
    }

    internal suspend fun finnAdressebeskyttelse(fødselsnummer: String, behovId: String): Adressebeskyttelse? =
        retry(
            "pdl_hent_person",
            IOException::class,
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) { pdl.finnAdressebeskyttelse(fødselsnummer, behovId) }

    private suspend fun finnGeografiskTilknytning(fødselsnummer: String, behovId: String): GeografiskTilknytning? =
        retry(
            "pdl_hent_geografisktilknytning",
            IOException::class,
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) { pdl.finnGeografiskTilhørighet(fødselsnummer, behovId) }

}
