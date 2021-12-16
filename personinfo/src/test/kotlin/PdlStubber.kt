import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.helse.sparkel.personinfo.*
import no.nav.helse.sparkel.personinfo.stubSts
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal abstract class PdlStubber {

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    protected val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    protected lateinit var personinfoService: PersoninfoService

    protected fun stubPdlRespons(body: String, callId:String = "behovId") {
        WireMock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo("/graphql"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .withHeader("Nav-Call-Id", WireMock.equalTo(callId))

                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                )
        )
    }

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        WireMock.configureFor(WireMock.create().port(wireMockServer.port()).build())
        stubSts()
        personinfoService = PersoninfoService(
            PdlClient(
                baseUrl = "${wireMockServer.baseUrl()}/graphql",
                stsClient = StsRestClient(
                    baseUrl = wireMockServer.baseUrl(),
                    serviceUser = ServiceUser("", "")
                )
            )
        )
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }
}