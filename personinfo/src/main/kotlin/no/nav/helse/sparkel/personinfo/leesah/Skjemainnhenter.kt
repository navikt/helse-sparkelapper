package no.nav.helse.sparkel.personinfo.leesah

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URL
import org.apache.avro.Schema
import org.slf4j.LoggerFactory

internal class Skjema(
    internal val schema: Schema,
    internal val versjon: String) {
    override fun equals(other: Any?) = other is Skjema && other.schema == schema && other.versjon == versjon
    override fun hashCode() = schema.hashCode() + versjon.hashCode()
}

internal object Skjemainnhenter {

    internal fun hentSkjema(url: URL, version: (json: JsonNode) -> String = { it.path("version").asText() }): Skjema {
        val json = url.readText().somJsonNode()
        val schema =  when (json.hasNonNull("schema")) {
            true -> json.skjema()
            false -> Schema.Parser().parse("$json")
        }
        val versjon = version(json)
        logger.info("Hentet skjema på versjon $versjon fra $url")
        return Skjema(schema, versjon)
    }

    private val logger = LoggerFactory.getLogger(Skjemainnhenter::class.java)
    private fun String.somJsonNode() = jacksonObjectMapper().readTree(this)
    private fun JsonNode.skjema() = Schema.Parser().parse(path("schema").asText())
    internal fun String.resourceUrl() =
        Skjemainnhenter::class.java.getResource(this)!!
}