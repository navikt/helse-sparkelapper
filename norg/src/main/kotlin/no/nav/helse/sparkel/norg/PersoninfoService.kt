package no.nav.helse.sparkel.norg

import java.io.IOException

class PersoninfoService(private val norg2Client: Norg2Client, private val pdl: PDL) {
    suspend fun finnBehandlendeEnhet(fødselsnummer: String, behovId: String): String {
        val diskresjonskode = requireNotNull(finnPerson(fødselsnummer, behovId)).adressebeskyttelse.kode
        val geografiskTilknytning =
            requireNotNull(finnGeografiskTilknytning(fødselsnummer, behovId))
        return norg2Client.finnBehandlendeEnhet(geografiskTilknytning.mestNøyaktig(), diskresjonskode).enhetNr
    }

    internal suspend fun finnPerson(fødselsnummer: String, behovId: String): Person? =
        retry(
            "pdl_hent_person",
            IOException::class,
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) { pdl.finnPerson(fødselsnummer, behovId) }

    private suspend fun finnGeografiskTilknytning(fødselsnummer: String, behovId: String): GeografiskTilknytning? =
        retry(
            "pdl_hent_geografisktilknytning",
            IOException::class,
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) { pdl.finnGeografiskTilhørighet(fødselsnummer, behovId) }

}
