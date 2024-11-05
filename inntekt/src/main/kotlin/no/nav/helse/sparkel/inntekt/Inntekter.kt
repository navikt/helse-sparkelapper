package no.nav.helse.sparkel.inntekt

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asYearMonth
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import com.github.navikt.tbd_libs.rapids_and_rivers.withMDC
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.inntekt.Inntekter.Type.InntekterForSammenligningsgrunnlag
import no.nav.helse.sparkel.inntekt.Inntekter.Type.InntekterForSykepengegrunnlag
import no.nav.helse.sparkel.inntekt.Inntekter.Type.InntekterForSykepengegrunnlagForArbeidsgiver
import org.slf4j.LoggerFactory
import java.time.YearMonth
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.sparkel.inntekt.Inntekter.Type.InntekterForOpptjeningsvurdering

class Inntekter(
    rapidsConnection: RapidsConnection,
    private val inntektsRestClient: InntektRestClient
) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        Sykepengegrunnlag(rapidsConnection)
        SykepengegrunnlagForArbeidsgiver(rapidsConnection)
        Sammenligningsgrunnlag(rapidsConnection)
        Opptjeningsvurdering(rapidsConnection)
    }

    enum class Type(val ainntektfilter: String) {
        InntekterForSykepengegrunnlag("8-28"),
        InntekterForSykepengegrunnlagForArbeidsgiver("8-28"),
        InntekterForSammenligningsgrunnlag("8-30"),
        InntekterForOpptjeningsvurdering("8-30")
    }

    inner class Opptjeningsvurdering(rapidsConnection: RapidsConnection) :
        River.PacketListener {

        init {
            River(rapidsConnection).apply {
                validate { it.demandAll("@behov", listOf(InntekterForOpptjeningsvurdering.name)) }
                validate { it.requireKey("@id", "fødselsnummer") }
                validate { it.interestedIn("vedtaksperiodeId") }
                validate { it.require("${InntekterForOpptjeningsvurdering.name}.beregningStart", JsonNode::asYearMonth) }
                validate { it.require("${InntekterForOpptjeningsvurdering.name}.beregningSlutt", JsonNode::asYearMonth) }
                validate { it.rejectKey("@løsning") }
            }.register(this)
        }

        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            this@Inntekter.onOpptjeningsvurderingPacket(packet, context)
        }

        override fun onError(problems: MessageProblems, context: MessageContext) {
            log.error(problems.toString())
        }
    }

    inner class Sykepengegrunnlag(rapidsConnection: RapidsConnection) :
        River.PacketListener {

        init {
            River(rapidsConnection).apply {
                validate { it.demandAll("@behov", listOf(InntekterForSykepengegrunnlag.name)) }
                validate { it.requireKey("@id", "fødselsnummer") }
                validate { it.interestedIn("vedtaksperiodeId") }
                validate { it.require("${InntekterForSykepengegrunnlag.name}.beregningStart", JsonNode::asYearMonth) }
                validate { it.require("${InntekterForSykepengegrunnlag.name}.beregningSlutt", JsonNode::asYearMonth) }
                validate { it.rejectKey("@løsning") }
            }.register(this)
        }

        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            this@Inntekter.onSykepengegrunnlagPacket(packet, context)
        }

        override fun onError(problems: MessageProblems, context: MessageContext) {
            log.error(problems.toString())
        }
    }

    inner class SykepengegrunnlagForArbeidsgiver(rapidsConnection: RapidsConnection) :
        River.PacketListener {

        init {
            River(rapidsConnection).apply {
                validate { it.demandAll("@behov", listOf(InntekterForSykepengegrunnlagForArbeidsgiver.name)) }
                validate { it.requireKey("@id", "fødselsnummer") }
                validate { it.requireKey("vedtaksperiodeId") }
                validate { it.requireKey("${InntekterForSykepengegrunnlagForArbeidsgiver.name}.organisasjonsnummer") }
                validate { it.require("${InntekterForSykepengegrunnlagForArbeidsgiver.name}.beregningStart", JsonNode::asYearMonth) }
                validate { it.require("${InntekterForSykepengegrunnlagForArbeidsgiver.name}.beregningSlutt", JsonNode::asYearMonth) }
                validate { it.rejectKey("@løsning") }
            }.register(this)
        }

        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            this@Inntekter.onSykepengegrunnlagForArbeidsgiverPacket(packet, context)
        }

        override fun onError(problems: MessageProblems, context: MessageContext) {
            log.error(problems.toString())
        }
    }

    inner class Sammenligningsgrunnlag(rapidsConnection: RapidsConnection) :
        River.PacketListener {

        init {
            River(rapidsConnection).apply {
                validate { it.demandAll("@behov", listOf(InntekterForSammenligningsgrunnlag.name)) }
                validate { it.requireKey("@id", "fødselsnummer") }
                validate { it.interestedIn("vedtaksperiodeId") }
                validate { it.require("${InntekterForSammenligningsgrunnlag.name}.beregningStart", JsonNode::asYearMonth) }
                validate { it.require("${InntekterForSammenligningsgrunnlag.name}.beregningSlutt", JsonNode::asYearMonth) }
                validate { it.rejectKey("@løsning") }
            }.register(this)
        }

        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            this@Inntekter.onSammenligningsgrunnlagPacket(packet, context)
        }

        override fun onError(problems: MessageProblems, context: MessageContext) {
            log.error(problems.toString())
        }
    }

    private fun onSammenligningsgrunnlagPacket(
        packet: JsonMessage,
        context: MessageContext
    ) {
        withMDC(
            mapOf(
                "behovId" to packet["@id"].asText(),
                "vedtaksperiodeId" to packet["vedtaksperiodeId"].asText()
            )
        ) {
            val beregningStart = packet["${InntekterForSammenligningsgrunnlag.name}.beregningStart"].asYearMonth()
            val beregningSlutt = packet["${InntekterForSammenligningsgrunnlag.name}.beregningSlutt"].asYearMonth()

            hentInntekter(packet, InntekterForSammenligningsgrunnlag, beregningStart, beregningSlutt, context)
        }
    }

    private fun onSykepengegrunnlagPacket(
        packet: JsonMessage,
        context: MessageContext
    ) {
        withMDC(
            mapOf(
                "behovId" to packet["@id"].asText(),
                "vedtaksperiodeId" to packet["vedtaksperiodeId"].asText()
            )
        ) {
            val beregningStart = packet["${InntekterForSykepengegrunnlag.name}.beregningStart"].asYearMonth()
            val beregningSlutt = packet["${InntekterForSykepengegrunnlag.name}.beregningSlutt"].asYearMonth()

            hentInntekter(packet, InntekterForSykepengegrunnlag, beregningStart, beregningSlutt, context)
        }
    }

    private fun onSykepengegrunnlagForArbeidsgiverPacket(
        packet: JsonMessage,
        context: MessageContext
    ) {
        withMDC(
            mapOf(
                "behovId" to packet["@id"].asText(),
                "vedtaksperiodeId" to packet["vedtaksperiodeId"].asText()
            )
        ) {
            val orgnr = packet["${InntekterForSykepengegrunnlagForArbeidsgiver.name}.organisasjonsnummer"].asText()
            val beregningStart = packet["${InntekterForSykepengegrunnlagForArbeidsgiver.name}.beregningStart"].asYearMonth()
            val beregningSlutt = packet["${InntekterForSykepengegrunnlagForArbeidsgiver.name}.beregningSlutt"].asYearMonth()

            hentInntekter(packet, InntekterForSykepengegrunnlagForArbeidsgiver, beregningStart, beregningSlutt, context, orgnr)
        }
    }
    private fun onOpptjeningsvurderingPacket(
        packet: JsonMessage,
        context: MessageContext
    ) {
        withMDC(
            mapOf(
                "behovId" to packet["@id"].asText(),
                "vedtaksperiodeId" to packet["vedtaksperiodeId"].asText()
            )
        ) {
            val beregningStart = packet["${InntekterForOpptjeningsvurdering.name}.beregningStart"].asYearMonth()
            val beregningSlutt = packet["${InntekterForOpptjeningsvurdering.name}.beregningSlutt"].asYearMonth()

            hentInntekter(packet, InntekterForOpptjeningsvurdering, beregningStart, beregningSlutt, context)
        }
    }

    private fun hentInntekter(
        packet: JsonMessage,
        type: Type,
        beregningStart: YearMonth,
        beregningSlutt: YearMonth,
        context: MessageContext,
        orgnr: String? = null
    ) {
        try {
            val callId = if (packet["vedtaksperiodeId"].isMissingOrNull()) UUID.randomUUID().toString().also {
                log.info("Genererer en ny callId: $it i hentInntekter")
            } else packet["vedtaksperiodeId"].asText()

            log.info("Behandler behov {}", kv("id", packet["@id"].asText()))
            packet["@løsning"] = mapOf<String, Any>(
                type.name to runBlocking {
                    inntektsRestClient.hentInntektsliste(
                        fnr = packet["fødselsnummer"].asText(),
                        fom = beregningStart,
                        tom = beregningSlutt,
                        filter = type.ainntektfilter,
                        callId = "$callId-${packet["@id"].asText()}",
                        orgnummer = orgnr
                    )
                })
            context.publish(packet.toJson().also {
                log.info("løser behov: {}", keyValue("id", packet["@id"].asText()))
                sikkerlogg.info("svarer behov {} med {}", keyValue("id", packet["@id"].asText()), it)
            })
        } catch (e: ResponseException) {
            log.warn("Feilet ved løsing av behov: ${e.message}", e)
            runBlocking {
                sikkerlogg.warn(
                    "Feilet ved løsing av behov: ${e.message}\n\t${e.response.bodyAsText()}",
                    e
                )
            }
        } catch (e: Exception) {
            log.warn("Feilet ved løsing av behov: ${e.message}", e)
            sikkerlogg.warn("Feilet ved løsing av behov: ${e.message}", e)
        }
    }
}
