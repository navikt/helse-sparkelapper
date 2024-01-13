package no.nav.helse.sparkel.arena

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.navikt.tbd_libs.soap.MinimalSoapClient
import com.github.navikt.tbd_libs.soap.SoapAssertionStrategy
import com.github.navikt.tbd_libs.soap.deserializeSoapBody
import java.time.LocalDate
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

class MeldekortUtbetalingsgrunnlagClient(
    private val soapClient: MinimalSoapClient,
    private val assertionStrategy: SoapAssertionStrategy
) {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }
    private val mapper = FinnMeldekortUtbetalingsgrunnlagListeResponse.bodyHandler()

    fun hentMeldekortutbetalingsgrunnlag(tema: String, ident: String, fom: LocalDate, tom: LocalDate): FinnMeldekortUtbetalingsgrunnlagListeResponse {
        val response = executeHttpRequest(createXmlBody(tema, ident, fom, tom))
        sikkerlogg.info("RESPONSE FRA ARENA:\n$response")
        return deserializeSoapBody(mapper, response)
    }

    private fun executeHttpRequest(requestBody: String): String {
        return soapClient.doSoapAction(
            action = "http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/meldekortUtbetalingsgrunnlag_v1/finnMeldekortUtbetalingsgrunnlagListeRequest",
            body = requestBody,
            tokenStrategy = assertionStrategy
        )
    }

    private fun createXmlBody(tema: String, ident: String, fom: LocalDate, tom: LocalDate): String {
        @Language("XML")
        val requestBody = """<ns2:finnMeldekortUtbetalingsgrunnlagListe xmlns:ns2="http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1">
    <request>
        <ident xsi:type="ns4:Bruker" xmlns:ns4="http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <ident>$ident</ident>
        </ident>
        <periode>
            <fom>$fom</fom>
            <tom>$tom</tom>
        </periode>
        <temaListe>$tema</temaListe>
    </request>
</ns2:finnMeldekortUtbetalingsgrunnlagListe>"""
        return requestBody
    }
}


data class FinnMeldekortUtbetalingsgrunnlagListeResponse(
    @JacksonXmlProperty(
        localName = "finnMeldekortUtbetalingsgrunnlagListeResponse",
        namespace = "http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1"
    )
    val finnMeldekortResponse: FinnMeldekortResponse
) {
    companion object {
        const val NAME_FOR_TEXT_ELEMENT = "innerText"
        // en objectmapper som funker til å deserialisere dataklassen;
        // dvs. den er konfigurert til å matche kravene til dataklassen.
        // Fordi det varierer mellom soap-tjeneste til soap-tjeneste så
        // er det nok overkill å skulle tilby én felles-funker-for-alle-objectMapper
        fun bodyHandler(): ObjectMapper {
            return XmlMapper.builder()
                .addModules(JavaTimeModule())
                .addModule(kotlinModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // issue: https://github.com/FasterXML/jackson-module-kotlin/issues/138
                // workaround: https://github.com/FasterXML/jackson-module-kotlin/issues/138#issuecomment-576484905
                .nameForTextElement(NAME_FOR_TEXT_ELEMENT)
                .build()
        }
    }
}

data class FinnMeldekortResponse(
    @JacksonXmlProperty(localName = "response")
    val response: FinnMeldekortResponseResponse?
)

data class FinnMeldekortResponseResponse(
    @JacksonXmlProperty(localName = "meldekortUtbetalingsgrunnlagListe")
    @JacksonXmlElementWrapper(useWrapping = false)
    val meldekortUtbetalingsgrunnlagListe: List<ArenaSak> = emptyList()
)

data class ArenaSak(
    @JacksonXmlProperty(localName = "fagsystemSakId")
    val fagsystemSakId: String,

    @JacksonXmlProperty(localName = "saksstatus")
    val saksstatus: ArenaSaksstatus,

    @JacksonXmlProperty(localName = "tema")
    val tema: ArenaTema,

    @JacksonXmlProperty(localName = "vedtakListe")
    @JacksonXmlElementWrapper(useWrapping = false)
    val vedtaksliste: List<ArenaVedtak>
)

data class ArenaSaksstatus(
    @JacksonXmlProperty(isAttribute = true, localName = "termnavn")
    val termnavn: String,
    @JacksonXmlProperty(localName = FinnMeldekortUtbetalingsgrunnlagListeResponse.NAME_FOR_TEXT_ELEMENT)
    val verdi: String
)

data class ArenaTema(
    @JacksonXmlProperty(isAttribute = true, localName = "termnavn")
    var termnavn: String,
    // issue: https://github.com/FasterXML/jackson-module-kotlin/issues/138
    // workaround: https://github.com/FasterXML/jackson-module-kotlin/issues/138#issuecomment-576484905
    @JacksonXmlProperty(localName = "innerText")
    val verdi: String
)

data class ArenaVedtak(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "meldekortListe")
    val meldekortliste: List<ArenaMeldekort> = emptyList(),
    @JacksonXmlProperty(localName = "vedtaksperiode")
    val vedtaksperiode: ArenaPeriode,
    @JacksonXmlProperty(localName = "vedtaksstatus")
    val vedtaksstatus: ArenaVedtaksstatus,
    @JacksonXmlProperty(localName = "vedtaksdato")
    val vedtaksdato: LocalDate,
    @JacksonXmlProperty(localName = "datoKravMottatt")
    val datoKravMottatt: LocalDate,
    @JacksonXmlProperty(localName = "dagsats")
    val dagsats: Double
)

data class ArenaVedtaksstatus(
    @JacksonXmlProperty(isAttribute = true, localName = "termnavn")
    val termnavn: String,
    @JacksonXmlProperty(localName = "innerText")
    val verdi: String
)

data class ArenaMeldekort(
    @JacksonXmlProperty(localName = "meldekortperiode")
    val meldekortperiode: ArenaPeriode,
    @JacksonXmlProperty(localName = "dagsats")
    val dagsats: Double,
    @JacksonXmlProperty(localName = "beloep")
    val beløp: Double,
    @JacksonXmlProperty(localName = "utbetalingsgrad")
    val utbetalingsgrad: Double
)

data class ArenaPeriode(
    @JacksonXmlProperty(localName = "fom")
    val fom: LocalDate?,
    @JacksonXmlProperty(localName = "tom")
    val tom: LocalDate?
)