package no.nav.helse.sparkel.sigrun

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.JacksonConverter
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asYearMonth
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
                this.level = LogLevel.ALL
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
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .validate {
                it.demandAll("@behov", listOf("InntekterForSykepengegrunnlag"))
                it.rejectKey("@løsning")
                it.requireKey("fødselsnummer")
                it.require("InntekterForSykepengegrunnlag.beregningStart", JsonNode::asYearMonth)
            }
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val beregningÅr = packet["InntekterForSykepengegrunnlag.beregningStart"].asYearMonth().year
        val fnr = packet["fødselsnummer"].asText()

        log.info("henter beregnet skatt for person")
        sikkerlog.info("henter beregnet skatt for $fnr")
        (beregningÅr - 4).until(beregningÅr).forEach { år ->
            log.info("Henter beregnet skatt for $år")
            sikkerlog.info("Henter beregnet skatt for $år")
            sigrunClient.hentBeregnetSkatt(fnr, år)
        }
    }
}

private class SigrunClient(
    private val sigrunBaseUrl: String,
    private val httpClient: HttpClient,
    private val tokenClient: AccessTokenClient,
    private val scope: String
) {
    fun hentBeregnetSkatt(fnr: String, år: Int) {
        val accessToken = runBlocking { tokenClient.hentAccessToken(scope) } ?: return
        val response: BeregnetSkattResponse = runBlocking {
            httpClient.preparePost(sigrunBaseUrl + "/api/beregnetskatt") {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                accessToken.berikRequestMedBearer(headers)
                header("x-naturligident", fnr)
                header("x-aktoerid", "")
                header("x-filter", "BeregnetSkattPensjonsgivendeInntekt")
                header("x-inntektsaar", "$år")
                header("Nav-Call-Id", "${UUID.randomUUID()}")
                header("Nav-Consumer-Id", "sparkel-sigrun")
            }.body()
        }

        sikkerlog.info("respons fra Sigrun:\n\t${mapper.writeValueAsString(response)}")
    }

    private class BeregnetSkattResponse(
        val opplysninger: List<BeregnetSkattOpplysning>
    ) {
        class BeregnetSkattOpplysning(
            private val tekniskNavn: String,
            private val verdi: String
        )
    }
}