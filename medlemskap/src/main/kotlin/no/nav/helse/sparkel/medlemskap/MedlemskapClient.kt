package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

internal class MedlemskapClient(
    private val baseUrl: URI,
    private val azureClient: AzureTokenProvider,
    private val scope: String
) {

    internal fun hentMedlemskapsvurdering(fnr: String, fom: LocalDate, tom: LocalDate): JsonNode {
        val (responseCode, responseBody) =
            with(URI("$baseUrl/speilvurdering").toURL().openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                val bearerToken = azureClient.bearerToken(scope).getOrThrow()
                setRequestProperty("Authorization", "Bearer ${bearerToken.token}")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 10_000
                readTimeout = 30_000
                doOutput = true
                outputStream.use {
                    it.bufferedWriter().apply {
                        val requestBody = byggRequest(fnr, fom, tom)
                        sikkerlogg.info("Sender $requestBody", keyValue("fødselsnummer", fnr))
                        write(requestBody)
                        flush()
                    }
                }

                val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
                responseCode to stream?.bufferedReader()?.readText()
            }

        if (responseBody == null) {
            throw MedlemskapException("unknown error (responseCode=$responseCode) from medlemskap", responseBody)
        }
        val responseJson = objectMapper.readTree(responseBody)
        if (responseCode >= 300) {
            throw MedlemskapException("unknown error (responseCode=$responseCode) from medlemskap", responseBody)
        }

        return parseSvar(objectMapper, responseJson)
    }

    internal companion object {
        private val objectMapper = jacksonObjectMapper()
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        @Language("JSON")
        private fun byggRequest(fnr: String, fom: LocalDate, tom: LocalDate) = """
        {
          "fnr": "$fnr",
          "periode": {"fom": "$fom", "tom": "$tom" },
          "ytelse": "SYKEPENGER",
          "førsteDagForYtelse": "$fom"
        }
        """

        fun parseSvar(objectMapper: ObjectMapper, root: JsonNode): JsonNode {
            return objectMapper.createObjectNode().set("resultat", objectMapper.createObjectNode().put("svar", finnSvar(root)))
        }

        private fun finnSvar(root: JsonNode): String {
            if (root.path("resultat").path("svar").isTextual) return root.path("resultat").path("svar").asText()

            if (root.path("speilSvar").isTextual) return root.path("speilSvar").asText()

            throw MedlemskapException("Klarte ikke parse medlemsskapsrespone", root.toString())
        }
    }
}

internal class MedlemskapException(message: String, val responseBody: String?) : RuntimeException(message)
