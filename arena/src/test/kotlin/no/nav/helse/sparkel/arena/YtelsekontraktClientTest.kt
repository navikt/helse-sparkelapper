package no.nav.helse.sparkel.arena

import com.github.navikt.tbd_libs.mock.MockHttpResponse
import com.github.navikt.tbd_libs.soap.MinimalSoapClient
import com.github.navikt.tbd_libs.soap.SamlToken
import com.github.navikt.tbd_libs.soap.SamlTokenProvider
import io.mockk.every
import io.mockk.mockk
import java.net.URI
import java.net.http.HttpClient
import java.time.LocalDate
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class YtelsekontraktClientTest {

    @Test
    fun `mapper tom respons fra arena`() {
        @Language("XML")
        val response = """<ns2:hentYtelseskontraktListeResponse xmlns:ns2="http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3">
    <response>
        <bruker>
            <rettighetsgruppe>
                <rettighetsGruppe>Ingen livsoppholdsytelse i Arena</rettighetsGruppe>
            </rettighetsgruppe>
        </bruker>
    </response>
</ns2:hentYtelseskontraktListeResponse>"""

        val (_, meldekortClient) = mockClient(xmlResponse(response))
        val result = meldekortClient.hentYtelsekontrakt("12345678911", LocalDate.EPOCH, LocalDate.EPOCH)
        assertEquals(emptyList<Unit>(), result.hentYtelseskontraktListeResponse.response.ytelseskontraktListe)
    }


    @Test
    fun `mapper vedtak`() {
        @Language("XML")
        val response = """<ns2:hentYtelseskontraktListeResponse xmlns:ns2="http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3">
    <response>
        <bruker>
            <rettighetsgruppe>
                <rettighetsGruppe>Arbeidsavklaringspenger</rettighetsGruppe>
            </rettighetsgruppe>
        </bruker>
        <ytelseskontraktListe fomGyldighetsperiode="2018-01-01T00:00:00"
                              tomGyldighetsperiode="2018-12-31T00:00:00">
            <datoKravMottatt>2018-01-01</datoKravMottatt>
            <fagsystemSakId>123456789</fagsystemSakId>
            <status>Aktiv</status>
            <ytelsestype>Arbeidsavklaringspenger</ytelsestype>
            <ihtVedtak>
                <beslutningsdato>2018-01-01</beslutningsdato>
                <periodetypeForYtelse>Ny rettighet</periodetypeForYtelse>
                <uttaksgrad>100</uttaksgrad>
                <vedtakBruttoBeloep>50000</vedtakBruttoBeloep>
                <vedtakNettoBeloep>50000</vedtakNettoBeloep>
                <vedtaksperiode>
                    <fom>2018-01-01</fom>
                    <tom>2018-12-31</tom>
                </vedtaksperiode>
                <status>Iverksatt</status>
                <vedtakstype>Arbeidsavklaringspenger / Ny rettighet</vedtakstype>
                <aktivitetsfase>Under arbeidsavklaring</aktivitetsfase>
                <dagsats>1500</dagsats>
            </ihtVedtak>
        </ytelseskontraktListe>
    </response>
</ns2:hentYtelseskontraktListeResponse>"""

        val (_, meldekortClient) = mockClient(xmlResponse(response))
        val result = meldekortClient.hentYtelsekontrakt("12345678911", LocalDate.EPOCH, LocalDate.EPOCH)
        val vedtaksperiode = result.hentYtelseskontraktListeResponse.response.ytelseskontraktListe.single().vedtaksliste.single().vedtaksperiode
        assertEquals(LocalDate.of(2018, 1, 1), vedtaksperiode.fom)
        assertEquals(LocalDate.of(2018, 12, 31), vedtaksperiode.tom)
    }

    private fun xmlResponse(body: String): String {
        @Language("XML")
        val response = """<?xml version='1.0' encoding='UTF-8'?>
<S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/">
    <S:Header>
        <Action xmlns="http://www.w3.org/2005/08/addressing">an action</Action>
        <MessageID xmlns="http://www.w3.org/2005/08/addressing">a message id</MessageID>
        <RelatesTo xmlns="http://www.w3.org/2005/08/addressing">an uuid</RelatesTo>
        <To xmlns="http://www.w3.org/2005/08/addressing">http://www.w3.org/2005/08/addressing/anonymous</To>
    </S:Header>
    <S:Body>$body</S:Body>
</S:Envelope>"""
        return response
    }

    private fun mockClient(response: String): Pair<HttpClient, YtelsekontraktClient> {
        val httpClient = mockk<HttpClient> {
            every {
                send<String>(any(), any())
            } returns MockHttpResponse(response)
        }
        val tokenProvider = object : SamlTokenProvider {
            override fun samlToken(username: String, password: String): SamlToken {
                throw NotImplementedError("ikke implementert i mock")
            }
        }
        val soapClient = MinimalSoapClient(URI("http://ytelsekontrakt-ws"), tokenProvider, httpClient)
        val client = YtelsekontraktClient(
            soapClient = soapClient,
            assertionStrategy = { "<saml token>" }
        )
        return httpClient to client
    }
}