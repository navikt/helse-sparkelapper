import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.sparkel.personinfo.*
import no.nav.helse.sparkel.personinfo.stubSts
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
        stubSts()

        val accessTokenClient = mockk<AccessTokenClient>(relaxed = true)
        coEvery { accessTokenClient.hentAccessToken(any()) } returns "1234abc"

        personinfoService = PersoninfoService(
            PdlClient(
                baseUrl = "${wireMockServer.baseUrl()}/graphql",
                accessTokenClient = accessTokenClient,
                accessTokenScope = "someScope"
            )
        )
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }

    @Language("Json")
    internal fun utenVergemålOgFullmakt() = """
        {
          "data": {
            "hentPerson": {
              "vergemaalEllerFremtidsfullmakt": [],
              "fullmakt": []
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
              ],
              "fullmakt": []
            }
          }
        }
    """.trimIndent()

    @Language("Json")
    internal fun medFullmakt(områder: List<String> = listOf("SYK")) = """
        {
          "data": {
            "hentPerson": {
              "vergemaalEllerFremtidsfullmakt": [],
              "fullmakt": [
                {
                  "gyldigFraOgMed": "2021-12-01",
                  "gyldigTilOgMed": "2022-12-30",
                  "omraader": ${områder.map { """"$it"""" }}
                }
              ]
            }
          }
        }
    """.trimIndent()
}