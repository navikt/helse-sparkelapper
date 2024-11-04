import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.ok
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.mockk.mockk
import java.time.LocalDateTime
import no.nav.helse.sparkel.personinfo.PdlClient
import no.nav.helse.sparkel.personinfo.PersoninfoService
import org.intellij.lang.annotations.Language
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

    protected fun stubPdlRespons(body: String) {
        WireMock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo("/graphql"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .withHeader("Nav-Call-Id", AnythingPattern())

                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                )
        )
    }

    @BeforeAll
    fun setupWiremock() {
        wireMockServer.start()
        WireMock.configureFor(WireMock.create().port(wireMockServer.port()).build())

        personinfoService = PersoninfoService(
            PdlClient(
                baseUrl = "${wireMockServer.baseUrl()}/graphql",
                accessTokenClient = object : AzureTokenProvider {
                    override fun bearerToken(scope: String) = AzureToken("1234abc", LocalDateTime.MAX).ok()
                    override fun onBehalfOfToken(scope: String, token: String): Result<AzureToken> {
                        throw NotImplementedError("ikke implementert i mocken")
                    }
                },
                accessTokenScope = "someScope"
            ),
            mockk(relaxed = true),
        )
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }

    @Language("Json")
    internal fun utenVergemålEllerFremtidsfullmakt() = """
        {
          "data": {
            "hentPerson": {
              "vergemaalEllerFremtidsfullmakt": []
            }
          }
        }
    """.trimIndent()

    internal fun medFremtidsfullmakt() = medVergemål(type = "stadfestetFremtidsfullmakt")

    @Language("Json")
    internal fun medVergemål(type: String = "voksen") = """
        {
          "data": {
            "hentPerson": {
              "vergemaalEllerFremtidsfullmakt": [
                {
                  "type": "$type"
                }
              ]
            }
          }
        }
    """.trimIndent()
}