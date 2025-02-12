package no.nav.helse.sparkel.sigrun

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asYearMonth
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.getOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.micrometer.core.instrument.MeterRegistry
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.sigrun.SigrunClient.BeregnetSkattOpplysning.Companion.tilMap
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("no.nav.helse.sparkel.sigrun.App")
private val sikkerlog = LoggerFactory.getLogger("tjenestekall")

private val mapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        val httpClient = HttpClient(Apache) {
            install(ContentNegotiation) {
                register(
                    ContentType.Application.Json, JacksonConverter(mapper)
                )
            }
            install(Logging) {
                this.level = LogLevel.NONE
                this.logger = object : Logger {
                    override fun log(message: String) {
                        sikkerlog.info(message)
                    }
                }
            }
        }

        val tokenClient = createAzureTokenClientFromEnvironment(env)
        PensjonsgivendeInntekt(this, SigrunClient(
            env.getValue("SIGRUN_URL"),
            httpClient,
            tokenClient,
            env.getValue("ACCESS_TOKEN_SCOPE")
        ))
    }
}

private class PensjonsgivendeInntekt(
    rapidsConnection: RapidsConnection,
    private val sigrunClient: SigrunClient
) {
    init {
        River(rapidsConnection)
            .precondition {
                it.requireAll("@behov", listOf("InntekterForSykepengegrunnlag"))
                it.forbid("@løsning")
            }
            .validate {
                it.requireKey("fødselsnummer")
                it.require("InntekterForSykepengegrunnlag.beregningStart", JsonNode::asYearMonth)
            }
            .register(VilkårsgrunnlagRiver())
        River(rapidsConnection)
            .precondition { it.requireValue("@event_name", "sigrun_test") }
            .validate {
                it.requireKey("fødselsnummer", "beregningÅr")
            }
            .register(TestRiver())
    }

    private inner class VilkårsgrunnlagRiver : River.PacketListener {
        override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
            val beregningÅr = packet["InntekterForSykepengegrunnlag.beregningStart"].asYearMonth().year
            val fnr = packet["fødselsnummer"].asText()
            hentBeregnetSkatt(fnr, beregningÅr)
        }
    }
    private inner class TestRiver : River.PacketListener {
        override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
            val beregningÅr = packet["beregningÅr"].asInt()
            val fnr = packet["fødselsnummer"].asText()
            hentBeregnetSkatt(fnr, beregningÅr)
        }
    }

    private fun hentBeregnetSkatt(fnr: String, beregningÅr: Int) {
        log.info("henter beregnet skatt for person")
        sikkerlog.info("henter beregnet skatt for $fnr")

        val result = mutableMapOf<Int, Map<String, Int>>()
        (beregningÅr - 4).until(beregningÅr).forEach { år ->
            log.info("Henter beregnet skatt for $år")
            sikkerlog.info("Henter beregnet skatt for $år")
            result[år] = sigrunClient.hentBeregnetSkatt(fnr, år)
        }

        sikkerlog.info("respons fra Sigrun:\n\t${mapper.writeValueAsString(result.map { (år, opplysninger) ->
            mapOf(
                "år" to år,
                "opplysninger" to opplysninger
            )
        })}")
    }
}

private class SigrunClient(
    private val sigrunBaseUrl: String,
    private val httpClient: HttpClient,
    private val tokenClient: AzureTokenProvider,
    private val scope: String
) {
    fun hentBeregnetSkatt(fnr: String, år: Int): Map<String, Int> {
        return runBlocking {
            val response = httpClient.prepareGet("$sigrunBaseUrl/api/beregnetskatt") {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                val bearerToken = tokenClient.bearerToken(scope).getOrThrow()
                bearerAuth(bearerToken.token)
                header("x-naturligident", fnr)
                header("x-aktoerid", "")
                header("x-filter", "BeregnetSkattPensjonsgivendeInntekt")
                header("x-inntektsaar", "$år")
                val callId = UUID.randomUUID()
                header("Nav-Call-Id", "$callId")
                header("no.nav.callid", "$callId")
                header("Nav-Consumer-Id", "sparkel-sigrun")
                header("no.nav.consumer.id", "sparkel-sigrun")
            }.execute()
            if (response.status != HttpStatusCode.OK) {
                "Sigrun svarte med http ${response.status.value}, returnerer derfor tomt resultat".also {
                    log.info(it)
                    sikkerlog.info(it)
                }
                emptyMap()
            }
            else response.body<List<BeregnetSkattOpplysning>>().tilMap()
        }
    }

    class BeregnetSkattOpplysning(
        private val tekniskNavn: String,
        private val verdi: String
    ) {
        companion object {
            fun List<BeregnetSkattOpplysning>.tilMap() = this
                .associateBy(BeregnetSkattOpplysning::tekniskNavn, BeregnetSkattOpplysning::verdi)
                .mapValues {
                    try { it.value.toInt() }
                    catch (err: NumberFormatException) {
                        "${it.value} lar seg ikke tolke som Int: ${err.message}".also { errortekst ->
                            log.warn(errortekst, err)
                            sikkerlog.warn(errortekst, err)
                        }
                        null
                    }
                }
                .filterValues { it != null }
                .mapValues { it.value!! }
        }
    }
}