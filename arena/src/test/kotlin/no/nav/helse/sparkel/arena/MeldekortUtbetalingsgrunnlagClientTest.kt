package no.nav.helse.sparkel.arena

import com.github.navikt.tbd_libs.mock.MockHttpResponse
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.ok
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class MeldekortUtbetalingsgrunnlagClientTest {

    @Test
    fun `mapper tom respons fra arena`() {
        @Language("XML")
        val response = """<ns2:finnMeldekortUtbetalingsgrunnlagListeResponse xmlns:ns2="https://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1">
    <response/>
</ns2:finnMeldekortUtbetalingsgrunnlagListeResponse>"""

        val (_, meldekortClient) = mockClient(xmlResponse(response))
        val result = meldekortClient.hentMeldekortutbetalingsgrunnlag("AAP", "12345678911", LocalDate.EPOCH, LocalDate.EPOCH)
        assertEquals(emptyList<Unit>(), result.finnMeldekortResponse.response?.meldekortUtbetalingsgrunnlagListe)
    }

    @Test
    fun `mapper liste av vedtak`() {
        @Language("XML")
        val response = """<ns2:finnMeldekortUtbetalingsgrunnlagListeResponse xmlns:ns2="https://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1">
    <response>
        <meldekortUtbetalingsgrunnlagListe>
            <vedtakListe>
                <vedtaksperiode>
                    <fom>2018-01-01</fom>
                    <tom>2018-01-31</tom>
                </vedtaksperiode>
                <vedtaksstatus termnavn="Avsluttet">AVSLU</vedtaksstatus>
                <vedtaksdato>2018-01-01</vedtaksdato>
                <datoKravMottatt>2018-01-01</datoKravMottatt>
                <dagsats>1000.0</dagsats>
            </vedtakListe>
            <vedtakListe>
                <vedtaksperiode>
                    <fom>2019-01-01</fom>
                    <tom>2019-12-31</tom>
                </vedtaksperiode>
                <vedtaksstatus termnavn="Iverksatt">IVERK</vedtaksstatus>
                <vedtaksdato>2019-01-01</vedtaksdato>
                <datoKravMottatt>2019-01-01</datoKravMottatt>
                <dagsats>1000.0</dagsats>
            </vedtakListe>
            <fagsystemSakId>123456789</fagsystemSakId>
            <saksstatus termnavn="Aktiv">AKTIV</saksstatus>
            <tema termnavn="AAP">AAP</tema>
        </meldekortUtbetalingsgrunnlagListe>
    </response>
</ns2:finnMeldekortUtbetalingsgrunnlagListeResponse>"""

        val (_, meldekortClient) = mockClient(xmlResponse(response))
        val result = meldekortClient.hentMeldekortutbetalingsgrunnlag("AAP", "12345678911", LocalDate.EPOCH, LocalDate.EPOCH)
        assertEquals(2, result.finnMeldekortResponse.response?.meldekortUtbetalingsgrunnlagListe?.single()?.vedtaksliste?.size)
    }

    @Test
    fun `mapper verdier som kan være null`() {
        @Language("XML")
        val response = """<ns2:finnMeldekortUtbetalingsgrunnlagListeResponse xmlns:ns2="https://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1">
    <response>
        <meldekortUtbetalingsgrunnlagListe>
            <vedtakListe>
                <vedtaksperiode/>
                <vedtaksstatus termnavn="Avsluttet">AVSLU</vedtaksstatus>
                <datoKravMottatt>2018-01-01</datoKravMottatt>
                <dagsats>0.0</dagsats>
            </vedtakListe>
            <fagsystemSakId>1234567</fagsystemSakId>
            <saksstatus termnavn="Inaktiv">INAKT</saksstatus>
            <tema termnavn="Dagpenger">DAG</tema>
        </meldekortUtbetalingsgrunnlagListe>
    </response>
</ns2:finnMeldekortUtbetalingsgrunnlagListeResponse>"""

        val (_, meldekortClient) = mockClient(xmlResponse(response))
        val result = meldekortClient.hentMeldekortutbetalingsgrunnlag("DAG", "12345678911", LocalDate.EPOCH, LocalDate.EPOCH)
        val arenaSak = result.finnMeldekortResponse.response?.meldekortUtbetalingsgrunnlagListe?.single() ?: fail { "Forventet en sak" }
        assertEquals(1, arenaSak.vedtaksliste.size)
        val arenaVedtak = arenaSak.vedtaksliste.single()
        assertNull(arenaVedtak.vedtaksperiode.fom)
        assertNull(arenaVedtak.vedtaksperiode.tom)
        assertNull(arenaVedtak.vedtaksdato)
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

    private fun mockClient(response: String, statusCode: Int = 200): Pair<HttpClient, MeldekortUtbetalingsgrunnlagClient> {
        val httpClient = mockk<HttpClient> {
            every {
                send<String>(any(), any())
            } returns MockHttpResponse(response, statusCode)
        }
        val tokenProvider = object : SamlTokenProvider {
            override fun samlToken(username: String, password: String): Result<SamlToken> {
                throw NotImplementedError("ikke implementert i mock")
            }
        }
        val soapClient = MinimalSoapClient(URI("http://meldekort-ws"), tokenProvider, httpClient)
        val client = MeldekortUtbetalingsgrunnlagClient(
            soapClient = soapClient,
            assertionStrategy = { "<saml token>".ok() }
        )
        return httpClient to client
    }
}