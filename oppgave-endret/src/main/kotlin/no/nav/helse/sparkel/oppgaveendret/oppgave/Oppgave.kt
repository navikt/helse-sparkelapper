package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.sparkel.oppgaveendret.oppgave.Identtype.FOLKEREGISTERIDENT
import org.slf4j.LoggerFactory

enum class Identtype {
    // FNR eller DNR
    FOLKEREGISTERIDENT,
    NPID,
    ORGNR,
    SAMHANDLERNR
}

data class Oppgave(
    internal val id: Long,
    private val tema: String,
    internal val ident: String,
    private val identtype: Identtype
) {
    internal fun erRelevant() = tema == relevantTema && identtype == FOLKEREGISTERIDENT

    companion object {
        private const val relevantTema = "SYK"
        private val logg = LoggerFactory.getLogger(Oppgave::class.java)

        fun fromJson(jsonNode: JsonNode): Oppgave? {
            val oppgaveId = jsonNode.path("oppgave").path("oppgaveId").asLong()
            val brukerJson = jsonNode.path("oppgave").path("bruker") ?: kotlin.run {
                logg.info("Mangler bruker for oppgave med {}", kv("oppgaveId", oppgaveId))
                return null
            }
            return Oppgave(
                oppgaveId,
                jsonNode.path("oppgave").path("kategorisering").path("tema").asText(),
                brukerJson.path("ident").asText(),
                enumValueOf(jsonNode.path("oppgave").path("bruker").path("identType").asText()),
            )
        }
    }
}