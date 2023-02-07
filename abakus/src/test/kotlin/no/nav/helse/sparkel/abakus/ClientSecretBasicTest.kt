package no.nav.helse.sparkel.abakus

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ClientSecretBasicTest {

    private lateinit var server: WireMockServer
    private lateinit var client: ClientSecretBasic

    @BeforeAll
    fun beforeAll() {
        server = WireMockServer(WireMockConfiguration.options().dynamicPort())
        server.start()
        WireMock.configureFor(server.port())
        mockSts()
        client = ClientSecretBasic(
            clientId = "foo",
            clientSecret = "bar",
            tokenEndpoint = server.stsTokenEndpoint()
        )
    }

    @Test
    fun `hente access token`() {
        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik1uQ19WWmNBVGZNNXBP...", client.accessToken())
    }

    @AfterAll
    fun afterAll() = server.stop()

    internal companion object {
        @Language("JSON")
        private val tokenResponse = """
        {
          "token_type": "Bearer",
          "expires_in": 3599,
          "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik1uQ19WWmNBVGZNNXBP..."
        }
        """
        internal fun WireMockServer.stsTokenEndpoint() = "${baseUrl()}/token"
        internal fun mockSts() {
            WireMock.stubFor(
                WireMock.get(WireMock.urlPathMatching("/token"))
                    .withHeader("Authorization", WireMock.equalTo("Basic Zm9vOmJhcg==")) // foo:bar
                    .withQueryParam("grant_type", WireMock.equalTo("client_credentials"))
                    .withQueryParam("scope", WireMock.equalTo("openid"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withBody(tokenResponse)
                    )
            )
        }
    }
}