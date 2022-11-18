package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.SocketTimeoutException
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

internal class WorkaroundHandler {
    private val readTimedOutPersoner = mutableSetOf<String>()

    internal fun handle(fnr: String, fom: LocalDate, tom: LocalDate, block: () -> Pair<Int, String?>): Pair<Int, String?> {
        try {
            val (responseCode, responseBody) = block()
            sikkerlogg.info("Svar fra Lovme: responseCode=$responseCode responseBody=$responseBody", keyValue("fødselsnummer", fnr))
            return when (responseBody.jsonNode().erGradertExceptionMelding()) {
                true -> 200 to byggUavklart(fnr, fom, tom)
                false -> responseCode to responseBody
            }
        } catch (ste: SocketTimeoutException) {
            if (ste.message == ReadTimeout && readTimedOutPersoner.contains(fnr)) {
                readTimedOutPersoner.remove(fnr)
                sikkerlogg.info("Svarer uavklart medlemskap etter gjentatte $ReadTimeout mot Lovme for {}", keyValue("fødselsnummer", fnr))
                return 200 to byggUavklart(fnr, fom, tom)
            }
            else {
                readTimedOutPersoner.add(fnr)
                throw ste
            }
        }
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

        private const val ReadTimeout = "Read timed out"

        private fun String?.jsonNode() = when (this == null) {
            true -> objectMapper.createObjectNode()
            false -> try { objectMapper.readTree(this) } catch (e: Exception) { objectMapper.createObjectNode()}
        }

        private fun JsonNode.erGradertExceptionMelding() = path("cause").isTextual && path("cause").asText().lowercase().contains("GradertAdresseException".lowercase())

        @Language("JSON")
        private fun byggUavklart(fnr: String, fom: LocalDate, tom: LocalDate) = """
        {
            "resultat": {
                "datagrunnlag": {
                    "fnr": "$fnr",
                    "periode": {
                        "fom": "$fom",
                        "tom": "$tom"
                    }
                },
                "svar": "UAVKLART"
            }
        }
        """
    }
}