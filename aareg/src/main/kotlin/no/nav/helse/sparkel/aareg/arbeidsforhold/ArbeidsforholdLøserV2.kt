package no.nav.helse.sparkel.aareg.arbeidsforhold

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import java.util.UUID
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.AaregArbeidsforhold
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.Arbeidsstedtype.Underenhet
import org.slf4j.LoggerFactory

class ArbeidsforholdLøserV2(rapidsConnection: RapidsConnection, private val aaregClient: AaregClient) :
    River.PacketListener {

    companion object {
        internal fun List<AaregArbeidsforhold>.toArbeidsforhold(): List<Arbeidsforhold> = this.map { arbeidsforhold ->
            Arbeidsforhold(
                ansattSiden = arbeidsforhold.ansettelsesperiode.startdato,
                ansattTil = arbeidsforhold.ansettelsesperiode.sluttdato,
                orgnummer = arbeidsforhold.arbeidssted.run { if (type == Underenhet) getOrgnummer() else "" },
                type = Arbeidsforholdtype.valueOf(arbeidsforhold.type.kode.name),
            )
        }
    }

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.requireAllOrAny("@behov", listOf("ArbeidsforholdV2", "AlleArbeidsforhold")) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("Mottok melding: ${packet.toJson()}")

        val arbeidsforhold = try {
            log.info("løser behov={}", keyValue("id", packet["@id"].asText()))
            runBlocking {
                aaregClient
                    .hentFraAareg(packet["fødselsnummer"].asText(), UUID.fromString(packet["@id"].asText()))
                    .toArbeidsforhold()
            }
        } catch (exception: UkjentIdentException) {
            log.warn("Ignorerer behov={}, personen finnes ikke", packet["@id"].asText())
            return
        } catch (err: AaregException) {
            log.error(
                "Feilmelding for behov={} ved oppslag i AAreg. Ignorerer behov",
                keyValue("id", packet["@id"].asText())
            )
            sikkerlogg.error(
                "Feilmelding for behov={} ved oppslag i AAreg: ${err.message}. Ignorerer behov. Response:\n\t{}",
                keyValue("id", packet["@id"].asText()),
                err.responseValue(),
                err
            )
            return
        } catch (err: ClientRequestException) {
            log.warn(
                "Feilmelding for behov={} ved oppslag i AAreg. Svarer med tom liste",
                keyValue("id", packet["@id"].asText())
            )
            sikkerlogg.warn(
                "Feilmelding for behov={} ved oppslag i AAreg: ${err.message}. Svarer med tom liste. Response: {}",
                keyValue("id", packet["@id"].asText()),
                runBlocking { err.response.bodyAsText() },
                err
            )
            emptyList()
        }

        val behov = packet["@behov"].first { it.asText() in listOf("ArbeidsforholdV2", "AlleArbeidsforhold") }.asText()
        packet.setLøsning(behov, arbeidsforhold)
        context.publish(packet.toJson())
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        super.onSevere(error, context)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {

    }

    private fun JsonMessage.setLøsning(nøkkel: String, arbeidsforhold: List<Arbeidsforhold>) {
        this["@løsning"] = mapOf(
            nøkkel to arbeidsforhold
        )
    }
}
