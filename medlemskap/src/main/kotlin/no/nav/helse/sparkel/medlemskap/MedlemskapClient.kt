package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import org.intellij.lang.annotations.Language

internal class MedlemskapClient(
    private val baseUrl: String,
    private val azureClient: AzureClient,
    private val accesstokenScope: String = "??"
) {

    internal companion object {
        private val objectMapper = ObjectMapper()
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")
        fun oversett(response: JsonNode, fnr: String, fom: LocalDate, tom: LocalDate): JsonNode {
            if (response.erGradertExceptionMelding()) {
                return objectMapper.readTree(byggUavklart(fnr, fom, tom))
            }
            return response
        }

        private fun JsonNode.erGradertExceptionMelding() = path("cause").isTextual && path("cause").asText().lowercase().contains("GradertAdresseException".lowercase())
    }
    internal fun hentMedlemskapsvurdering(fnr: String, fom: LocalDate, tom: LocalDate): JsonNode {
        val (responseCode, responseBody) = with(URL(baseUrl).openConnection() as HttpURLConnection) {
            requestMethod = "POST"

            setRequestProperty("Authorization", "Bearer ${azureClient.getToken(accesstokenScope).accessToken}")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
            doOutput = true
            outputStream.use {
                it.bufferedWriter().apply {
                    val requestBody = byggRequest(fnr, fom, tom)
                    tjenestekallLog.info("Sender $requestBody")
                    write(requestBody)
                    flush()
                }
            }

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        tjenestekallLog.info("svar fra medlemskap: url=$baseUrl responseCode=$responseCode responseBody=$responseBody")

        if (responseBody == null) {
            throw MedlemskapException("unknown error (responseCode=$responseCode) from medlemskap", responseBody)
        }
        val responseJson = objectMapper.readTree(responseBody)
        if (responseCode >= 300 && !responseJson.erGradertExceptionMelding()) {
            throw MedlemskapException("unknown error (responseCode=$responseCode) from medlemskap", responseBody)
        }

        return oversett(responseJson, fnr, fom, tom)
    }

    @Language("JSON")
    private fun byggRequest(fnr: String, fom: LocalDate, tom: LocalDate) = """{
            "fnr": "$fnr",
            "periode": {"fom": "$fom", "tom": "$tom" },
            "ytelse": "SYKEPENGER",
            "førsteDagForYtelse": "$fom"
        }""".trimIndent()
}

internal class MedlemskapException(message: String, val responseBody: String?) : RuntimeException(message)

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
   "svar": "VetIkke"
 }
}
""".trimIndent()