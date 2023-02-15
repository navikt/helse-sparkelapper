package no.nav.helse.sparkel.sputnik

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FpsakRestClient(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val stsRestClient: StsRestClient
): Foreldrepengerløser {
    suspend fun hentGjeldendeForeldrepengeytelse(aktørId: String): Ytelse? =
        hentYtelse(aktørId, "$baseUrl/fpsak/api/vedtak/gjeldendevedtak-foreldrepenger")
            .map { it.toYtelse() }
            .firstOrNull()

    suspend fun hentGjeldendeSvangerskapsytelse(aktørId: String): Ytelse? =
        hentYtelse(aktørId, "$baseUrl/fpsak/api/vedtak/gjeldendevedtak-svangerskapspenger")
            .map { it.toYtelse() }
            .firstOrNull()

    override suspend fun hent(aktørId: String, fom: LocalDate, tom: LocalDate) = Foreldrepengerløsning(
        Foreldrepengeytelse = hentGjeldendeForeldrepengeytelse(aktørId),
        Svangerskapsytelse = hentGjeldendeSvangerskapsytelse(aktørId)
    )

    private suspend fun hentYtelse(aktørId: String, url: String) =
        httpClient.prepareGet(url) {
            header("Authorization", "Bearer ${stsRestClient.token()}")
            accept(ContentType.Application.Json)
            parameter("aktoerId", aktørId)
        }.execute { objectMapper.readValue<ArrayNode>(it.bodyAsText()) }

    private fun JsonNode.toYtelse() = Ytelse(
        aktørId = this["aktør"]["verdi"].textValue(),
        fom = this["periode"]["fom"].let { LocalDate.parse(it.textValue()) },
        tom = this["periode"]["tom"].let { LocalDate.parse(it.textValue()) },
        vedtatt = this["vedtattTidspunkt"].let {
            LocalDateTime.parse(
                it.textValue(),
                DateTimeFormatter.ISO_DATE_TIME
            )
        },
        perioder = mapPerioder(this)
    )

    private fun mapPerioder(ytelse: JsonNode): List<Periode> = (ytelse["anvist"] as ArrayNode).map { periode ->
        Periode(
            fom = periode["periode"]["fom"].let { LocalDate.parse(it.textValue()) },
            tom = periode["periode"]["tom"].let { LocalDate.parse(it.textValue()) }
        )
    }
}