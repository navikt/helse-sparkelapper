package no.nav.helse.sparkel.norg

import com.github.navikt.tbd_libs.result_object.getOrThrow
import com.github.navikt.tbd_libs.speed.GeografiskTilknytningResponse
import com.github.navikt.tbd_libs.speed.PersonResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
import no.nav.helse.sparkel.retry

class PersoninfoService(private val norg2Client: Norg2Client, private val speedClient: SpeedClient) {
    suspend fun finnBehandlendeEnhet(fødselsnummer: String, behovId: String): String {
        val adresseBeskytellse = requireNotNull(finnAdressebeskyttelse(fødselsnummer, behovId)).norgkode
        val geografiskTilknytning =
            requireNotNull(finnGeografiskTilknytning(fødselsnummer, behovId))
        return norg2Client.finnBehandlendeEnhet(geografiskTilknytning.mestNøyaktig(), adresseBeskytellse).enhetNr
    }

    internal suspend fun finnAdressebeskyttelse(fødselsnummer: String, behovId: String): PersonResponse.Adressebeskyttelse? =
        retry(
            "pdl_hent_person",
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) {
            speedClient.hentPersoninfo(fødselsnummer, behovId).getOrThrow().adressebeskyttelse
        }

    private suspend fun finnGeografiskTilknytning(fødselsnummer: String, behovId: String): GeografiskTilknytningResponse? =
        retry(
            "pdl_hent_geografisktilknytning",
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) {
            speedClient.hentGeografiskTilknytning(fødselsnummer, behovId).getOrThrow()
        }


    private fun GeografiskTilknytningResponse.mestNøyaktig() = bydel ?: kommune ?: land ?: "ukjent"
    private val PersonResponse.Adressebeskyttelse.norgkode get() = when (this) {
        PersonResponse.Adressebeskyttelse.FORTROLIG -> "SPFO"
        PersonResponse.Adressebeskyttelse.STRENGT_FORTROLIG -> "SPSF"
        PersonResponse.Adressebeskyttelse.STRENGT_FORTROLIG_UTLAND -> "SPSF"
        PersonResponse.Adressebeskyttelse.UGRADERT -> ""
    }
}
