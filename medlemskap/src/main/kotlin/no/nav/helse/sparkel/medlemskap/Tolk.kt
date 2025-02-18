package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.retry.PredefinerteUtsettelser
import com.github.navikt.tbd_libs.retry.retryBlocking
import java.time.Duration.ofSeconds
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

data class Tolk(
    private val fødselsnummer: String,
    private val fom: LocalDate,
    private val tom: LocalDate,
    private val request: (requestBody: String) -> Pair<Int, String>
) {
    @Language("JSON")
    private val requestBody = """
    {
      "fnr": "$fødselsnummer",
      "periode": {"fom": "$fom", "tom": "$tom" },
      "ytelse": "SYKEPENGER",
      "førsteDagForYtelse": "$fom"
    }
    """

    private fun hentSvar(): Medlemskap {
        val (status, responseBody) = try { request(requestBody) } catch (exception: Exception) {
            throw Medlemskapsfeil(Medlemskap.Ubesvart(exception))
        }
        val jsonBody = responseBody.safeJson()
        if (jsonBody.path("resultat").path("svar").isTextual) return Medlemskap.Avklart(jsonBody.path("resultat").path("svar").asText())
        if (jsonBody.path("speilSvar").isTextual) return Medlemskap.SpeilAvklart(jsonBody.path("speilSvar").asText())
        if (status >= 500 && responseBody.contains("GradertAdresse")) return Medlemskap.Gradert(responseBody)
        throw Medlemskapsfeil(Medlemskap.Uventet(status, responseBody))
    }

    internal fun tolk(): String {
        val medlemskap = try {
            retryBlocking(Medlemskapsutsettelser()) { hentSvar() }
        } catch (medlemskapsfeil: Medlemskapsfeil) { medlemskapsfeil.medlemskap }

        medlemskap.logg(this)

        return when (medlemskap) {
            is Medlemskap.Avklart -> medlemskap.svar
            is Medlemskap.SpeilAvklart -> medlemskap.svar
            is Medlemskap.Gradert -> "UAVKLART"
            is Medlemskap.Ubesvart -> error("Fikk ikke kontakt med medlemskapstjenesten på 3 forsøk. Se sikkerlogg for detaljer")
            is Medlemskap.Uventet -> error("Fikk uventet svar fra medlemskapstjenesten 3 ganger. Se sikkerlogg for detaljer")
        }
    }

    private companion object {

        private sealed interface Medlemskap {
            fun logg(tolk: Tolk)

            data class Avklart(val svar: String): Medlemskap {
                override fun logg(tolk: Tolk) = sikkerlogg.info("Medlemskap for {} på ${tolk.fom} avklart til {} fra {}. \nRequestBody:\n\t${tolk.requestBody.jsonOrRaw()}",
                    keyValue("fødselsnummer", tolk.fødselsnummer),
                    keyValue("svar", svar),
                    keyValue("kilde", "VanligSvar")
                )
            }

            data class SpeilAvklart(val svar: String): Medlemskap {
                override fun logg(tolk: Tolk) = sikkerlogg.info("Medlemskap for {} på ${tolk.fom} avklart til {} fra {}. \nRequestBody:\n\t${tolk.requestBody.jsonOrRaw()}",
                    keyValue("fødselsnummer", tolk.fødselsnummer),
                    keyValue("svar", svar),
                    keyValue("kilde", "SpeilSvar")
                )
            }

            // https://github.com/navikt/medlemskap-oppslag/blob/300fa11f92c264cde64778254518794f1c9a41e8/src/main/kotlin/no/nav/medlemskap/common/ExceptionHandler.kt#L83
            data class Gradert(private val responseBody: String): Medlemskap {
                override fun logg(tolk: Tolk) = sikkerlogg.warn("Medlemskap for {} på ${tolk.fom} ikke vurdert. Sykmeldte er gradert. Defaulter til {}.\nRequestBody:\n\t${tolk.requestBody.jsonOrRaw()}\nResponseBody:\n\${responseBody.jsonOrRaw()}",
                    keyValue("fødselsnummer", tolk.fødselsnummer),
                    keyValue("svar", "UAVKLART"),
                    keyValue("kilde", "GradertSvar")
                )
            }

            data class Ubesvart(private val cause: Exception): Medlemskap {
                override fun logg(tolk: Tolk) = sikkerlogg.error("Medlemskap for {} på ${tolk.fom} ikke vurdert. Fikk ikke kontakt med medlemskapstjenesten.\nRequestBody:\n\t${tolk.requestBody.jsonOrRaw()}", cause,
                    keyValue("fødselsnummer", tolk.fødselsnummer)
                )
            }

            data class Uventet(private val status: Int, private val responseBody: String): Medlemskap {
                override fun logg(tolk: Tolk) = sikkerlogg.error("Medlemskap for {} på ${tolk.fom} ikke vurdert. Uventet svar fra medlemskapstjenesten. HTTP status $status\nRequestBody:\n\t${tolk.requestBody.jsonOrRaw()}\nResponseBody:\n\t${responseBody.jsonOrRaw()}",
                    keyValue("fødselsnummer", tolk.fødselsnummer)
                )
            }
        }

        private class Medlemskapsutsettelser: PredefinerteUtsettelser(ofSeconds(1), ofSeconds(3), ofSeconds(10))
        private class Medlemskapsfeil(val medlemskap: Medlemskap): RuntimeException()

        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val objectMapper = jacksonObjectMapper()
        private fun String.safeJson() = try { objectMapper.readTree(this) } catch (ignored: Exception) { objectMapper.createObjectNode() }
        private fun String.jsonOrRaw() = try { objectMapper.readTree(this).toString() } catch (ignored: Exception) { this }
    }
}
