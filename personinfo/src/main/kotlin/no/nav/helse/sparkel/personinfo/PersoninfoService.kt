package no.nav.helse.sparkel.personinfo

import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.error
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.retry.retryBlocking
import com.github.navikt.tbd_libs.speed.PersonResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
import com.github.navikt.tbd_libs.speed.VergemålEllerFremtidsfullmaktResponse
import org.slf4j.MDC

internal class PersoninfoService(private val speedClient: SpeedClient) {

    fun løsningForPersoninfo(callId: String, ident: String): Result<PersonResponse> {
        return try {
            retryBlocking {
                when (val svar = speedClient.hentPersoninfo(ident, callId)) {
                    is Result.Error -> throw RuntimeException(svar.error, svar.cause)
                    is Result.Ok -> svar.value.ok()
                }
            }
        } catch (err: Exception) {
            err.error(err.message ?: "Ukjent feil")
        }
    }

    fun løsningForVergemål(behovId: String, fødselsnummer: String): Result<VergemålEllerFremtidsfullmaktResponse> {
        return try {
            retryBlocking {
                when (val svar = speedClient.hentVergemålEllerFremtidsfullmakt(fødselsnummer, behovId)) {
                    is Result.Error -> throw RuntimeException(svar.error, svar.cause)
                    is Result.Ok -> svar.value.ok()
                }
            }
        } catch (err: Exception) {
            err.error(err.message ?: "Ukjent feil")
        }
    }
}