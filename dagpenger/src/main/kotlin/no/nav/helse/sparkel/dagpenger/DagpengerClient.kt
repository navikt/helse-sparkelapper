package no.nav.helse.sparkel.dagpenger

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import no.nav.helse.sparkel.retry
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.util.*
import javax.net.ssl.SSLHandshakeException

private val retryableExceptions =
    arrayOf(
        IOException::class,
        ClosedReceiveChannelException::class,
        SSLHandshakeException::class,
        SocketTimeoutException::class,
        ServerResponseException::class,
    )

class DagpengerClient(
    private val baseUrl: String,
    private val tokenClient: AzureTokenProvider,
    private val httpClient: HttpClient,
    private val scope: String,
) {
    suspend fun hentBeregninger(
        personidentifikator: String,
        fom: LocalDate,
        tom: LocalDate,
        behovId: String,
    ): Result<List<DagpengerBeregningResponse>> {
        val callId = UUID.randomUUID()
        return retry("meldekort", legalExceptions = retryableExceptions) {
            val response =
                httpClient
                    .preparePost("$baseUrl/dagpenger/datadeling/v1/beregninger") {
                        expectSuccess = true
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        val bearerToken = tokenClient.bearerToken(scope).getOrThrow()
                        bearerAuth(bearerToken.token)
                        setBody(
                            mapOf(
                                "personIdent" to personidentifikator,
                                "fraOgMedDato" to fom.toString(),
                                "tilOgMedDato" to tom.toString(),
                            ),
                        )
                        header("nav-callid", "$callId")
                        header("x-correlation-id", behovId)
                    }.execute()

            val utbetaltePerioder = response.body<List<DagpengerBeregningResponse>>().filter { it.utbetaltBeløp > 0 }
            Result.success(utbetaltePerioder)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DagpengerBeregningResponse(
        val fraOgMed: String,
        val tilOgMed: String,
        val utbetaltBeløp: Int,
    )
}
