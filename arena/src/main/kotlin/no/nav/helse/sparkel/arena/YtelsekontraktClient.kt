package no.nav.helse.sparkel.arena

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.navikt.tbd_libs.soap.MinimalSoapClient
import com.github.navikt.tbd_libs.soap.SoapAssertionStrategy
import com.github.navikt.tbd_libs.soap.SoapResponse
import com.github.navikt.tbd_libs.soap.SoapResponseHandlerException
import com.github.navikt.tbd_libs.soap.deserializeSoapBody
import java.time.LocalDate
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

class YtelsekontraktClient(
    private val soapClient: MinimalSoapClient,
    private val assertionStrategy: SoapAssertionStrategy
) {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }
    private val mapper = HentYtelseskontraktListeResponse.bodyHandler()

    fun hentYtelsekontrakt(ident: String, fom: LocalDate, tom: LocalDate): HentYtelseskontraktListeResponse {
        val response = executeHttpRequest(createXmlBody(ident, fom, tom))
        sikkerlogg.info("RESPONSE FRA ARENA:\n$response")
        return deserializeSoapBody(mapper, response)
    }

    private fun executeHttpRequest(requestBody: String): String {
        return soapClient.doSoapAction(
            action = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/Ytelseskontrakt_v3/hentYtelseskontraktListeRequest",
            body = requestBody,
            tokenStrategy = assertionStrategy
        )
    }

    private fun createXmlBody(ident: String, fom: LocalDate, tom: LocalDate): String {
        @Language("XML")
        val requestBody = """<ns2:hentYtelseskontraktListe xmlns:ns2="http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3">
    <request>
        <personidentifikator>$ident</personidentifikator>
        <periode>
            <fom>$fom</fom>
            <tom>$tom</tom>
        </periode>
    </request>
</ns2:hentYtelseskontraktListe>"""
        return requestBody
    }
}

data class HentYtelseskontraktListeResponse(
    @JacksonXmlProperty(
        localName = "hentYtelseskontraktListeResponse",
        namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3"
    )
    val hentYtelseskontraktListeResponse: HentYtelsekontaktResponse
) {
    companion object {
        fun bodyHandler(): ObjectMapper {
            return XmlMapper.builder()
                .addModules(JavaTimeModule())
                .addModule(kotlinModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build()
        }
    }
}

data class HentYtelsekontaktResponse(
    @JacksonXmlProperty(localName = "response")
    val response: HentYtelsekontraktResponseResponse
)

data class HentYtelsekontraktResponseResponse(
    @JacksonXmlProperty(localName = "ytelseskontraktListe")
    @JacksonXmlElementWrapper(useWrapping = false)
    val ytelseskontraktListe: List<Ytelsekontrakt> = emptyList()
)

data class Ytelsekontrakt(
    @JacksonXmlProperty(localName = "datoKravMottatt")
    val datoKravMottatt: LocalDate,

    @JacksonXmlProperty(localName = "fagsystemSakId")
    val fagsystemSakId: String,

    @JacksonXmlProperty(localName = "status")
    val status: String,

    @JacksonXmlProperty(localName = "ytelsestype")
    val ytelsestype: String,

    @JacksonXmlProperty(localName = "fomGyldighetsperiode")
    val fomGyldighetsperiode: LocalDate,

    @JacksonXmlProperty(localName = "tomGyldighetsperiode")
    val tomGyldighetsperiode: LocalDate,

    @JacksonXmlProperty(localName = "bortfallsprosentDagerIgjen")
    val bortfallsprosentDagerIgjen: Int,

    @JacksonXmlProperty(localName = "bortfallsprosentUkerIgjen")
    val bortfallsprosentUkerIgjen: Int,

    @JacksonXmlProperty(localName = "ihtVedtak")
    @JacksonXmlElementWrapper(useWrapping = false)
    val vedtaksliste: List<Ytelsevedtak>
)

data class Ytelsevedtak(
    @JacksonXmlProperty(localName = "beslutningsdato")
    val beslutningsdato: LocalDate,

    @JacksonXmlProperty(localName = "periodetypeForYtelse")
    val periodetypeForYtelse: String,

    @JacksonXmlProperty(localName = "uttaksgrad")
    val uttaksgrad: Int,

    @JacksonXmlProperty(localName = "vedtakBruttoBeloep")
    val vedtakBruttoBeløp: Int,

    @JacksonXmlProperty(localName = "vedtakNettoBeloep")
    val vedtakNettoBeløp: Int,

    @JacksonXmlProperty(localName = "vedtaksperiode")
    val vedtaksperiode: YtelsePeriode,

    @JacksonXmlProperty(localName = "status")
    val status: String,

    @JacksonXmlProperty(localName = "vedtakstype")
    val vedtakstype: String,

    @JacksonXmlProperty(localName = "aktivitetsfase")
    val aktivitetsfase: String,

    @JacksonXmlProperty(localName = "dagsats")
    val dagsats: Int
)

data class YtelsePeriode(
    @JacksonXmlProperty(localName = "fom")
    val fom: LocalDate,
    @JacksonXmlProperty(localName = "tom")
    val tom: LocalDate?
)