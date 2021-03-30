package no.nav.helse.sparkel.sykepengeperioder

import com.fasterxml.jackson.databind.JsonNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.sykepengeperioder.infotrygd.AzureClient
import no.nav.helse.sparkel.sykepengeperioder.infotrygd.InfotrygdClient
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDate
import java.time.LocalDateTime

@TestInstance(Lifecycle.PER_CLASS)
internal class SykepengehistorikkløserTest : H2Database() {

    private companion object {
        private val fnr = Fnr("14123456789")
        private const val orgnummer = "80000000"
    }

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    private lateinit var infotrygdService: InfotrygdService
    private val rapid = TestRapid()

    private val sisteSendtMelding get() = rapid.inspektør.message(rapid.inspektør.size.minus(1))

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        configureFor(create().port(wireMockServer.port()).build())
        stubAuthEndepunkt()
        infotrygdService = InfotrygdService(
            InfotrygdClient(
                baseUrl = wireMockServer.baseUrl(),
                accesstokenScope = "a_scope",
                azureClient = AzureClient(
                    tokenEndpoint = "${wireMockServer.baseUrl()}/token",
                    clientId = "client_id",
                    clientSecret = "client_secret"
                )
            ),
            dataSource
        )
        rapid.apply {
            Sykepengehistorikkløser(this, infotrygdService)
        }
    }

    @BeforeEach
    fun beforeEach() {
        stubSvarFraInfotrygd()
        rapid.reset()
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }

    @Test
    fun `løser behov`() {
        rapid.sendTestMessage(behov())
        val perioder = sisteSendtMelding.løsning()

        assertEquals(2, perioder.size)
    }

    @Test
    fun `ignorerer behov som er mer enn 30 minutter gamle`() {
        rapid.sendTestMessage(behov(opprettet = LocalDateTime.now().minusMinutes(35)))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `løser behov uten vedtaksperiodeId`() {
        val behov =
            """{"@id": "behovsid", "@opprettet":"${
                LocalDateTime.now().minusMinutes(1)
            }", "@behov":["${Sykepengehistorikkløser.behov}"], "${Sykepengehistorikkløser.behov}": { "historikkFom": "2016-01-01", "historikkTom": "2020-01-01"}, "fødselsnummer": "$fnr" }"""

        rapid.sendTestMessage(behov)

        val perioder = sisteSendtMelding.løsning()
        assertEquals(2, perioder.size)
    }

    @Test
    fun `mapper også ut inntekt og dagsats`() {
        rapid.sendTestMessage(behov())

        val perioder = sisteSendtMelding.løsning()

        assertEquals(2, perioder.size)

        assertSykeperiode(
            sykeperiode = perioder[0].utbetalteSykeperioder[0],
            fom = 5.september(2020),
            tom = 25.september(2020),
            grad = "100",
            orgnummer = orgnummer,
            dagsats = 2176.0
        )

        assertInntektsopplysninger(
            inntektsopplysninger = perioder[0].inntektsopplysninger,
            dato = 4.september(2020),
            inntektPerMåned = 565700 / 12,
            orgnummer = orgnummer
        )
        assertInntektsopplysninger(
            inntektsopplysninger = perioder[1].inntektsopplysninger,
            dato = 4.februar(2019),
            inntektPerMåned = 507680 / 12,
            orgnummer = orgnummer
        )
    }

    @Test
    fun `setter ikke statslønn hvis tidligere periode har statslønn`() {
        rapid.sendTestMessage(behov())
        sisteSendtMelding.løsning().let { utenStatslønn ->
            utenStatslønn.forEach { periode ->
                assertFalse(periode.statslønn)
            }
        }
    }

    @Test
    fun `setter statslønn hvis nyeste periode har statslønn`() {
        stubSvarFraInfotrygdMedAktivStatslønn()
        rapid.sendTestMessage(behov())

        sisteSendtMelding.løsning().let { medStatlønn ->
            assertTrue(medStatlønn[0].statslønn)
            assertFalse(medStatlønn[1].statslønn)
        }
    }

    @Test
    fun `setter arbeidsKategoriKode`() {
        rapid.sendTestMessage(behov())
        sisteSendtMelding.løsning().let { løsning ->
            assertEquals("01", løsning.first().arbeidsKategoriKode)
        }
    }

    @Test
    fun `forskjellig  arbeidsKategoriKode`() {
        stubSvarFraInfotrygdMedForskjelligarbeidsKategoriKode()
        rapid.sendTestMessage(behov())
        sisteSendtMelding.løsning().let { løsning ->
            assertEquals("01", løsning.first().arbeidsKategoriKode)
            assertEquals("02", løsning.last().arbeidsKategoriKode)
        }
    }

    private fun JsonNode.løsning() =
        this.path("@løsning").path(Sykepengehistorikkløser.behov).map {
            Utbetalingshistorikk(it)
        }

    private class Utbetalingshistorikk(json: JsonNode) {

        val utbetalteSykeperioder = json["utbetalteSykeperioder"].map {
            UtbetalteSykeperiode(it)
        }
        val inntektsopplysninger = json["inntektsopplysninger"].map {
            Inntektsopplysning(it)
        }
        val statslønn = json["statslønn"].asBoolean()
        val arbeidsKategoriKode = json["arbeidsKategoriKode"].asText()

        class UtbetalteSykeperiode(json: JsonNode) {
            val fom = json["fom"].asLocalDate()
            val tom = json["tom"].asLocalDate()
            val utbetalingsGrad = json["utbetalingsGrad"].asText()
            val orgnummer = json["orgnummer"].asText()
            val dagsats = json["dagsats"].asDouble()
        }

        class Inntektsopplysning(json: JsonNode) {
            val sykepengerFom = json["sykepengerFom"].asLocalDate()
            val inntekt = json["inntekt"].asInt()
            val orgnummer = json["orgnummer"].asText()
        }

        private companion object {
            fun JsonNode.asLocalDate() = LocalDate.parse(this.asText())
        }
    }

    private fun assertSykeperiode(
        sykeperiode: Utbetalingshistorikk.UtbetalteSykeperiode,
        fom: LocalDate,
        tom: LocalDate,
        grad: String,
        orgnummer: String,
        dagsats: Double
    ) {
        assertEquals(fom, sykeperiode.fom)
        assertEquals(tom, sykeperiode.tom)
        assertEquals(grad, sykeperiode.utbetalingsGrad)
        assertEquals(orgnummer, sykeperiode.orgnummer)
        assertEquals(dagsats, sykeperiode.dagsats)
    }

    private fun assertInntektsopplysninger(
        inntektsopplysninger: List<Utbetalingshistorikk.Inntektsopplysning>,
        dato: LocalDate,
        inntektPerMåned: Int,
        orgnummer: String
    ) {
        assertEquals(dato, inntektsopplysninger[0].sykepengerFom)
        assertEquals(inntektPerMåned, inntektsopplysninger[0].inntekt)
        assertEquals(orgnummer, inntektsopplysninger[0].orgnummer)
    }

    private fun stubAuthEndepunkt() {
        stubFor(
            post(urlMatching("/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                        "token_type": "Bearer",
                        "expires_in": 3599,
                        "access_token": "1234abc"
                    }"""
                        )
                )
        )
    }

    private fun stubSvarFraInfotrygdMedAktivStatslønn() {
        stubFor(
            get(urlPathEqualTo("/v1/hentSykepengerListe"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            getContents("statslønnAktivResponse.json")
                        )
                )
        )
    }

    private fun stubSvarFraInfotrygdMedForskjelligarbeidsKategoriKode() {
        stubFor(
            get(urlPathEqualTo("/v1/hentSykepengerListe"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            getContents("infotrygdResponseForskjelligArbeidsKategoriKode.json")
                        )
                )
        )
    }

    private fun stubSvarFraInfotrygd() {
        stubFor(
            get(urlPathEqualTo("/v1/hentSykepengerListe"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getContents("statslønnTidligereResponse.json"))
                )
        )
    }

    @Language("Json")
    private fun behov(opprettet: LocalDateTime = LocalDateTime.now().minusMinutes(5)) =
        """
            {
            "@id": "behovsid", 
            "@opprettet":"$opprettet",
            "@behov":[
                "${Sykepengehistorikkløser.behov}"], 
                "${Sykepengehistorikkløser.behov}": { 
                    "historikkFom": "2016-01-01", 
                    "historikkTom": "2020-01-01"
                }, 
                "fødselsnummer": "$fnr", 
                "vedtaksperiodeId": "id"
            }
        """

    private fun getContents(filename: String) = this::class.java.getResource("/$filename").readText()
}
