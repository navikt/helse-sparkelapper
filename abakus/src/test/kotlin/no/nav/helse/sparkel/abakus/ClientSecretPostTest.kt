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
internal class ClientSecretPostTest {

    private lateinit var server: WireMockServer
    private lateinit var client: ClientSecretPost

    @BeforeAll
    fun beforeAll() {
        server = WireMockServer(WireMockConfiguration.options().dynamicPort())
        server.start()
        WireMock.configureFor(server.port())
        mockTokenEndpoint()
        client = ClientSecretPost(
            clientId = "foo",
            clientSecret = "bar",
            tokenEndpoint = server.tokenEndpoint(),
            scope = "api://baz/.default"
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
        internal fun WireMockServer.tokenEndpoint() = "${baseUrl()}/token"
        internal fun mockTokenEndpoint() {
            WireMock.stubFor(
                WireMock.post(WireMock.urlPathMatching("/token"))
                    .withRequestBody(WireMock.equalTo("client_id=foo&client_secret=bar&scope=api://baz/.default&grant_type=client_credentials"))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(200)
                            .withBody(tokenResponse)
                    )
            )
        }
    }
}