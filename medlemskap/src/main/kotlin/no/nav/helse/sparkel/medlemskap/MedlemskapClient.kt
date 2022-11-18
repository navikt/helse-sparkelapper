package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    private val workaroundHandler = WorkaroundHandler()

    internal fun hentMedlemskapsvurdering(fnr: String, fom: LocalDate, tom: LocalDate): JsonNode {
        val (responseCode, responseBody) = workaroundHandler.handle(fnr, fom, tom) {
            with(URL(baseUrl).openConnection() as HttpURLConnection) {
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
        }

        tjenestekallLog.info("svar fra medlemskap: url=$baseUrl responseCode=$responseCode responseBody=$responseBody")

        if (responseBody == null) {
            throw MedlemskapException("unknown error (responseCode=$responseCode) from medlemskap", responseBody)
        }
        val responseJson = objectMapper.readTree(responseBody)
        if (responseCode >= 300) {
            throw MedlemskapException("unknown error (responseCode=$responseCode) from medlemskap", responseBody)
        }

        return responseJson
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")
        @Language("JSON")
        private fun byggRequest(fnr: String, fom: LocalDate, tom: LocalDate) = """
        {
          "fnr": "$fnr",
          "periode": {"fom": "$fom", "tom": "$tom" },
          "ytelse": "SYKEPENGER",
          "førsteDagForYtelse": "$fom"
        }
        """
    }
}

internal class MedlemskapException(message: String, val responseBody: String?) : RuntimeException(message)