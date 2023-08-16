package no.nav.helse.sparkel.sigrun

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asYearMonth
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

        val tokenClient = AccessTokenClient(
            aadAccessTokenUrl = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            clientId = env.getValue("AZURE_APP_CLIENT_ID"),
            clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET"),
            httpClient = httpClient
        )

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
            .validate {
                it.demandAll("@behov", listOf("InntekterForSykepengegrunnlag"))
                it.rejectKey("@løsning")
                it.requireKey("fødselsnummer")
                it.require("InntekterForSykepengegrunnlag.beregningStart", JsonNode::asYearMonth)
            }
            .register(VilkårsgrunnlagRiver())
        River(rapidsConnection)
            .validate {
                it.demandValue("@event_name", "sigrun_test")
                it.requireKey("fødselsnummer", "beregningÅr")
            }
            .register(TestRiver())
    }

    private inner class VilkårsgrunnlagRiver : River.PacketListener {
        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            val beregningÅr = packet["InntekterForSykepengegrunnlag.beregningStart"].asYearMonth().year
            val fnr = packet["fødselsnummer"].asText()
            hentBeregnetSkatt(fnr, beregningÅr)
        }
    }
    private inner class TestRiver : River.PacketListener {
        override fun onPacket(packet: JsonMessage, context: MessageContext) {
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
    private val tokenClient: AccessTokenClient,
    private val scope: String
) {
    fun hentBeregnetSkatt(fnr: String, år: Int): Map<String, Int> {
        val accessToken = runBlocking { tokenClient.hentAccessToken(scope) } ?: return emptyMap()
        return runBlocking {
            val response = httpClient.prepareGet("$sigrunBaseUrl/api/beregnetskatt") {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                accessToken.berikRequestMedBearer(headers)
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