package no.nav.helse.sparkel.sputnik

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.fullPath
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StsRestClientTest {
    private val baseUrl = "https://faktiskUrl"
    private val mockClient = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                if (!request.headers.contains("Authorization", "Basic c3J2c3B1dG5pazp5ZXNfc2VjdXJlIQ==")) {
                    error("Invalid credentials")
                }
                when (request.url.fullPath) {
                    "/rest/v1/sts/token?grant_type=client_credentials&scope=openid" -> {
                        respond("""{"access_token": "TOKEN", "token_type": "Bearer", "expires_in": 3600}""")
                    }
                    else -> error("Endepunktet finnes ikke ${request.url.fullPath}")
                }
            }
        }
    }
    private val stsClient = StsRestClient(baseUrl, ServiceUser("srvsputnik", "yes_secure!"), mockClient)

    @Test
    fun `skal parse token fra sts`() {
        val testToken = runBlocking { stsClient.token() }
        assertEquals("TOKEN", testToken)
    }
}
