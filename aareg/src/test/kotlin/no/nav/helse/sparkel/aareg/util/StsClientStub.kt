package no.nav.helse.sparkel.aareg.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.StsRestClient
import java.time.LocalDateTime
import java.time.ZoneOffset

internal val mockStsRestClient = StsRestClient(
    baseUrl = "",
    serviceUser = ServiceUser("yes", "yes"),
    httpClient = HttpClient(MockEngine) {
        engine {
            addHandler {
                val tokenExpirationTime = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                respondOk("""{"access_token":"token", "expires_in":$tokenExpirationTime, "token_type":"yes"}""")
            }
        }
    })
